package com.github.paganini2008.springdessert.logtracker.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 
 * FreemarkConfigurator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class FreemarkConfigurator implements EnvironmentPostProcessor {

	private final PropertiesPropertySourceLoader propertySourceLoader = new PropertiesPropertySourceLoader();

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Resource resource = new ClassPathResource("logtracker-freemarker.properties");
		PropertySource<?> freemarkerConfig;
		try {
			freemarkerConfig = propertySourceLoader.load("logtracker-freemarker", resource).get(0);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		environment.getPropertySources().addLast(freemarkerConfig);
	}

}
