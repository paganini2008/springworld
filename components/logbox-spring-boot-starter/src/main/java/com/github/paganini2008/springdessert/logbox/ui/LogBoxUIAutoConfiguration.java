package com.github.paganini2008.springdessert.logbox.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 
 * LogBoxUIAutoConfiguration
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Import({ PageController.class, LogEntryController.class })
@Configuration(proxyBeanMethods = false)
public class LogBoxUIAutoConfiguration {

	@Bean
	public WebMvcConfig webMvcConfig() {
		return new WebMvcConfig();
	}

}
