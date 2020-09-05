package com.github.paganini2008.springworld.config;

import static com.github.paganini2008.springworld.config.RemoteConfigurationSpringApplication.DEFAULT_BOOTSTRAP_CONFIG_NAME;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import com.github.paganini2008.devtools.NotImplementedException;
import com.github.paganini2008.devtools.StringUtils;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * ApplicationPropertiesConfig
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
@Configuration
@ConditionalOnResource(resources = { "classpath:settings.properties" })
@ConfigurationProperties(prefix = "spring.config")
public class ApplicationPropertiesConfig implements EnvironmentAware {

	private static final String DEFAULT_CONFIG_REPO = "git";
	private static final String REMOTE_APPLICATION_PROPERTIES_NAME = "remoteApplicationPropertiesConfig";

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.profiles.active}")
	private String env;

	private Environment environment;

	private String repo = DEFAULT_CONFIG_REPO;

	private Git git = new Git();

	@Bean(destroyMethod = "clearInterval")
	public ApplicationProperties applicationProperties() throws Exception {
		switch (repo) {
		case DEFAULT_CONFIG_REPO:
			return gitConfigProperties();
		case "svn":
		case "db":
			throw new NotImplementedException(repo);
		default:
			throw new UnsupportedOperationException("Unknown repo: " + repo);
		}
	}

	protected ApplicationProperties gitConfigProperties() throws Exception {
		GitConfigProperties configProperties = new GitConfigProperties();
		configProperties.setUrl(git.getUri());
		configProperties.setBranch(git.getBranch());
		configProperties.setUsername(git.getUsername());
		configProperties.setPassword(git.getPassword());
		configProperties.setSearchPath(git.getSearchPath());
		configProperties.setFileNames(StringUtils.isNotBlank(git.getFileNames()) ? git.getFileNames().split(",") : new String[0]);
		configProperties.setApplicationName(applicationName);
		configProperties.setUseDefaultSettings(git.isUseDefaultSettings());
		configProperties.setEnv(env);

		configProperties.setInterval(git.getRefreshingInterval(), TimeUnit.SECONDS);

		MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
		if (propertySources.contains(DEFAULT_BOOTSTRAP_CONFIG_NAME)) {
			propertySources.addBefore(DEFAULT_BOOTSTRAP_CONFIG_NAME,
					new PropertiesPropertySource(REMOTE_APPLICATION_PROPERTIES_NAME, configProperties));
		} else {
			propertySources.addLast(new PropertiesPropertySource(REMOTE_APPLICATION_PROPERTIES_NAME, configProperties));
		}
		return configProperties;
	}

	@Data
	public static class Git {

		private String uri;
		private String branch;
		private String username;
		private String password;
		private String searchPath;
		private String fileNames;
		private boolean useDefaultSettings;
		private int refreshingInterval = 1 * 60;

	}

}
