package com.github.paganini2008.springworld.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * ApplicationPropertiesKeeperConfig
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Configuration
public class ApplicationPropertiesKeeperConfig {

	@Bean
	public ApplicationPropertiesKeeper applicationPropertiesKeeper() {
		return new ApplicationPropertiesKeeper();
	}

}
