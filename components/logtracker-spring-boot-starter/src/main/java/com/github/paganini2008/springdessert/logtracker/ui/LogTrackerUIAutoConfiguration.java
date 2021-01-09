package com.github.paganini2008.springdessert.logtracker.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 
 * LogTrackerUIAutoConfiguration
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Import({ PageController.class, LogEntryController.class })
@Configuration(proxyBeanMethods = false)
public class LogTrackerUIAutoConfiguration {

	@Bean
	public WebMvcConfig webMvcConfig() {
		return new WebMvcConfig();
	}

}
