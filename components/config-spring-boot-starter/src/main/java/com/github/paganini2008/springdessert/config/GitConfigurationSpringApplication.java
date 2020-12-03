package com.github.paganini2008.springdessert.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.github.paganini2008.devtools.Console;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.io.FileUtils;
import com.github.paganini2008.devtools.io.PathUtils;

/**
 * 
 * GitConfigurationSpringApplication
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class GitConfigurationSpringApplication extends RemoteConfigurationSpringApplication {

	private static final String SOURCE_NAME_PREFIX = "gitcfg:";
	private static final String GLOBAL_SETTINGS_NAME = "default-settings";

	public GitConfigurationSpringApplication(Class<?>... mainClasses) {
		super(mainClasses);
	}

	protected void applySettings(String applicationName, String env, ConfigurableEnvironment environment) throws IOException {
		final boolean fetchLatest = environment.getProperty("spring.config.git.fetchLatest", Boolean.class, true);
		final boolean useDefaultSettings = environment.getProperty("spring.config.git.useDefaultSettings", Boolean.class, true);
		String url = environment.getProperty("spring.config.git.uri");
		String branch = environment.getProperty("spring.config.git.branch");
		String username = environment.getProperty("spring.config.git.username");
		String password = environment.getProperty("spring.config.git.password");
		String searchPath = environment.getProperty("spring.config.git.searchPath");
		String localRepoPath = environment.getProperty("spring.config.git.localRepoPath");
		String[] fileNames = environment.containsProperty("spring.config.git.fileNames")
				? environment.getProperty("spring.config.git.fileNames").split(",")
				: new String[0];
		File localRepo;
		if (StringUtils.isNotBlank(localRepoPath)) {
			localRepo = new File(localRepoPath);
		} else {
			localRepo = FileUtils.getFile(FileUtils.getUserDirectory(), ".gitcfg", applicationName);
		}

		if (fetchLatest) {
			boolean pull;
			Git git = null;
			boolean exists = FileUtils.mkdirs(localRepo);
			if (exists) {
				try {
					git = Git.open(localRepo);
					git.pull().setRemoteBranchName(branch)
							.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
					pull = true;
				} catch (Throwable ignored) {
					FileUtils.deleteDirectory(localRepo);
					pull = false;
				}
			} else {
				pull = false;
			}
			if (!pull) {
				FileUtils.mkdirs(localRepo);
				try {
					git = Git.cloneRepository().setURI(url).setBranch(branch).setDirectory(localRepo)
							.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
					pull = true;
				} catch (Throwable ignored) {
					pull = false;
				} finally {
					if (git != null) {
						git.close();
					}
				}
			}
			if (!pull) {
				throw new IOException("Can not pull configuration from git repository.");
			}
		} else if (!localRepo.exists()) {
			throw new IOException("Can not pull configuration from git repository.");
		}

		File[] fileArray = null;
		if (fileNames == null || fileNames.length == 0) {
			File searchDir = FileUtils.getFile(localRepo, searchPath, applicationName, env);
			if (searchDir.exists()) {
				fileArray = searchDir.listFiles((file) -> {
					final String fileName = file.getName().toLowerCase();
					return fileName.endsWith(".xml") || fileName.endsWith(".properties") || fileName.endsWith(".yml")
							|| fileName.endsWith(".yaml");
				});
			} else {
				Console.logf("[Warning] Configuration home '%s' doesn't exist and will be overwrited by default settings.", searchDir);
			}
		} else {
			fileArray = FileUtils.getFiles(fileNames.clone());
		}
		List<File> fileList = new ArrayList<File>();
		if (fileArray != null) {
			fileList.addAll(Arrays.asList(fileArray));
		}
		if (useDefaultSettings) {
			File globalConfigDir = FileUtils.getFile(localRepo, searchPath, GLOBAL_SETTINGS_NAME, env);
			if (globalConfigDir.exists()) {
				fileArray = globalConfigDir.listFiles((file) -> {
					final String fileName = file.getName().toLowerCase();
					return fileName.endsWith(".xml") || fileName.endsWith(".properties") || fileName.endsWith(".yml")
							|| fileName.endsWith(".yaml");
				});
				if (fileArray != null) {
					fileList.addAll(Arrays.asList(fileArray));
				}
			}
		}

		if (fileList.isEmpty()) {
			throw new IOException("No matched config files on this searchPath: " + searchPath);
		}
		sort(fileList);

		reconfigureEnvironment(fileList.toArray(new File[0]), environment);
	}

	protected void reconfigureEnvironment(File[] configFiles, ConfigurableEnvironment environment) throws IOException {
		for (File configFile : configFiles) {
			if (configFile.exists()) {
				PropertySourceLoader loader = null;
				String extension = PathUtils.getExtension(configFile.getName());
				switch (extension.toLowerCase()) {
				case "xml":
				case "properties":
					loader = new PropertiesPropertySourceLoader();
					break;
				case "yaml":
				case "yml":
					loader = new YamlPropertySourceLoader();
					break;
				}
				if (loader != null) {
					Resource resource = new FileSystemResource(configFile.getAbsolutePath());
					List<PropertySource<?>> sources = loader.load(SOURCE_NAME_PREFIX + configFile.getName(), resource);
					for (PropertySource<?> source : sources) {
						environment.getPropertySources().addLast(source);
					}
				}
			} else {
				Console.logf("[Warning] ConfigFile '%s' is not existed.", configFile);
			}
		}
	}

	protected void sort(List<File> files) {
		ApplicationPropertiesLoadingComparator.sort(files);
	}

	public static ConfigurableApplicationContext run(Class<?> mainClass, String[] args) {
		return new GitConfigurationSpringApplication(mainClass).run(args);
	}

}
