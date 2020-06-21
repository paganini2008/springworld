package com.github.paganini2008.springworld.cached;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.util.ErrorHandler;

import com.github.paganini2008.devtools.multithreads.ThreadPool;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springworld.cached.base.Cache;
import com.github.paganini2008.springworld.cached.base.LruCache;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationClusterCacheConfig
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@ConditionalOnProperty(value = "spring.application.cluster.cache.enabled", havingValue = "true")
@Configuration
public class ApplicationClusterCacheConfig {

	@Value("${spring.application.cluster.cache.maxSize:65536}")
	private int maxSize;

	@ConditionalOnMissingBean(Cache.class)
	@Bean
	public Cache cacheDelegate() {
		return new LruCache(maxSize);
	}

	@ConditionalOnMissingBean(ThreadPool.class)
	@Bean(destroyMethod = "shutdown")
	public ThreadPool taskThreadPool() {
		return ThreadUtils.commonPool();
	}

	@ConditionalOnMissingBean(ApplicationEventMulticaster.class)
	@Bean
	public ApplicationEventMulticaster applicationEventMulticaster() {
		SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
		eventMulticaster.setTaskExecutor(taskThreadPool());
		eventMulticaster.setErrorHandler(new DefaultErrorHandler());
		return eventMulticaster;
	}

	@Bean
	public ApplicationClusterCache applicationClusterCache() {
		return new ApplicationClusterCache();
	}

	@Bean
	public ApplicationClusterCacheEventProcessor applicationClusterCacheEventProcessor() {
		return new ApplicationClusterCacheEventProcessor();
	}

	@Bean
	public OperationNotificationEventListener operationNotificationEventListener() {
		return new OperationNotificationEventListener();
	}

	@Bean
	public CachedInvocationInterpreter cachedInvocationInterpreter() {
		return new CachedInvocationInterpreter();
	}

	@Slf4j
	public static class DefaultErrorHandler implements ErrorHandler {

		@Override
		public void handleError(Throwable e) {
			log.error(e.getMessage(), e);
		}

	}

}
