package com.github.paganini2008.springworld.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.springworld.cluster.consistency.ConsistencyRequestConfig;

/**
 * 
 * ApplicationClusterCacheConfig
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@ConditionalOnProperty(value = "spring.application.cluster.cache.enabled", havingValue = "true")
@ConditionalOnBean(ConsistencyRequestConfig.class)
@Configuration
public class ApplicationClusterCacheConfig {

	@Bean
	public ApplicationClusterCache applicationClusterCache() {
		return new ApplicationClusterCache(false);
	}

	@Bean
	public CachedInvocationInterpreter cachedInvocationInterpreter() {
		return new CachedInvocationInterpreter();
	}

}
