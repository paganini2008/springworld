package com.github.paganini2008.springdessert.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * ApplicationPropertiesKeeperConfig
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Configuration
public class ApplicationPropertiesKeeperConfig {

	@Bean
	public ApplicationPropertiesKeeper applicationPropertiesKeeper() {
		return new ApplicationPropertiesKeeper();
	}
	
	@Bean
	public InternalStringValueResolver internalStringValueResolver() {
		return new InternalStringValueResolver();
	}

}
