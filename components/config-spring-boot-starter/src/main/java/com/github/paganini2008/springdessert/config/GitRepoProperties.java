package com.github.paganini2008.springdessert.config;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.github.paganini2008.devtools.Console;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.io.FileUtils;
import com.github.paganini2008.devtools.io.IOUtils;

/**
 * 
 * GitRepoProperties
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class GitRepoProperties extends ApplicationProperties {

	private static final long serialVersionUID = 1601541498724403615L;
	private static final String GLOBAL_SETTINGS_NAME = "default-settings";

	private String url;
	private String branch;
	private String username;
	private String password;
	private String searchPath = "";
	private String[] fileNames;
	private String localRepoPath;
	private boolean useDefaultSettings;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSearchPath() {
		return searchPath;
	}

	public void setSearchPath(String searchPath) {
		this.searchPath = searchPath;
	}

	public String[] getFileNames() {
		return fileNames;
	}

	public void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}

	public String getLocalRepoPath() {
		return localRepoPath;
	}

	public void setLocalRepoPath(String localRepoPath) {
		this.localRepoPath = localRepoPath;
	}

	public boolean isUseDefaultSettings() {
		return useDefaultSettings;
	}

	public void setUseDefaultSettings(boolean useDefaultSettings) {
		this.useDefaultSettings = useDefaultSettings;
	}

	protected Properties createObject() throws Exception {
		File localRepo;
		if (StringUtils.isNotBlank(localRepoPath)) {
			localRepo = new File(localRepoPath);
		} else {
			localRepo = FileUtils.getFile(FileUtils.getUserDirectory(), ".gitcfg", applicationName);
		}

		boolean exists = FileUtils.mkdirs(localRepo);
		boolean pull;
		Git git = null;
		if (exists) {
			try {
				git = Git.open(localRepo);
				git.pull().setRemoteBranchName(branch).setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
						.call();
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
				ignored.printStackTrace();
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

		File[] fileArray = null;
		if (fileNames == null || fileNames.length == 0) {
			File searchDir = FileUtils.getFile(localRepo, searchPath, applicationName, env);
			if (searchDir.exists()) {
				fileArray = searchDir.listFiles((file) -> {
					final String fileName = file.getName().toLowerCase();
					return fileName.endsWith(".properties");
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
					return fileName.endsWith(".properties");
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

		final Properties p = new Properties();
		for (File cfgFile : fileList) {
			if (cfgFile.exists()) {
				Reader in = null;
				try {
					in = FileUtils.getBufferedReader(cfgFile, "UTF-8");
					p.load(in);
				} finally {
					IOUtils.closeQuietly(in);
				}
			}
		}
		return p;
	}

	protected void sort(List<File> files) {
		ApplicationPropertiesLoadingComparator.sort(files);
	}

}
