package com.github.paganini2008.springworld.cached;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

	@Value("${spring.application.cluster.cache.maxSize:65536}")
	private int maxSize;

	@ConditionalOnMissingBean(Cache.class)
	@Bean
	public Cache cacheDelegate() {
		return new LruCache(maxSize);
	}

	@Bean
	public ApplicationClusterCache applicationClusterCache() {
		return new ApplicationClusterCache(false);
	}

	@Bean
	public CachedInvocationInterpreter cachedInvocationInterpreter() {
		return new CachedInvocationInterpreter();
	}

}
