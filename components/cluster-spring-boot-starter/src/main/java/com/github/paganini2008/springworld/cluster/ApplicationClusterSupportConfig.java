package com.github.paganini2008.springworld.cluster;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * ApplicationClusterSupportConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
public class ApplicationClusterSupportConfig {

	@Bean
	@ConditionalOnMissingBean(InstanceIdGenerator.class)
	public InstanceIdGenerator instanceIdGenerator() {
		return new DefaultInstanceIdGenerator();
	}

	@Bean
	public InstanceId instanceId() {
		return new InstanceId();
	}

}
