package com.github.paganini2008.springworld.cluster;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.util.ErrorHandler;

import com.github.paganini2008.devtools.multithreads.ThreadPool;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;

import lombok.extern.slf4j.Slf4j;

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
	
	@Slf4j
	public static class DefaultErrorHandler implements ErrorHandler {

		@Override
		public void handleError(Throwable e) {
			log.error(e.getMessage(), e);
		}

	}

}
