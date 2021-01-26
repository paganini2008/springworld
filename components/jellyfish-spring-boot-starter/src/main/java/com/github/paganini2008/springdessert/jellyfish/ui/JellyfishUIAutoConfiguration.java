package com.github.paganini2008.springdessert.jellyfish.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 
 * JellyfishUIAutoConfiguration
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Import({ LogPage.class, LogEntryController.class, RealtimeStatisticController.class })
@Configuration(proxyBeanMethods = false)
public class JellyfishUIAutoConfiguration {

	@Bean
	public WebMvcConfig webMvcConfig() {
		return new WebMvcConfig();
	}

}
