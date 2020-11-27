package com.github.paganini2008.springdessert.cached;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import com.github.paganini2008.devtools.multithreads.ThreadPoolBuilder;
import com.github.paganini2008.springdessert.cached.base.Cache;
import com.github.paganini2008.springdessert.cached.base.LruCache;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationClusterCacheConfig
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@EnableAsync
@ConditionalOnExpression("${spring.application.cluster.multicast.enabled} && ${spring.application.cluster.cache.enabled}")
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

	@ConditionalOnMissingBean(AsyncConfigurer.class)
	@Bean
	public AsyncConfigurer cacheAsyncOperationConfig() {
		return new CacheAsyncOperationConfig();
	}

	@Slf4j
	public static class CacheAsyncOperationConfig implements AsyncConfigurer {

		@Value("${spring.application.cluster.cache.concurrents:8}")
		private int concurrents;

		@Override
		public Executor getAsyncExecutor() {
			return ThreadPoolBuilder.common(concurrents).setMaxPermits(concurrents).build();
		}

		@Override
		public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
			return new AsyncUncaughtExceptionHandler() {

				@Override
				public void handleUncaughtException(Throwable e, Method method, Object... params) {
					log.error(e.getMessage(), e);
				}
			};
		}

	}
}
