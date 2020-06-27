package com.github.paganini2008.springworld.cluster;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.util.ErrorHandler;

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

//	@ConditionalOnMissingBean(Executor.class)
//	@Bean(destroyMethod = "shutdown")
//	public Executor taskThreadPool() {
//		return Executors.newCachedThreadPool();
//	}
//
//	@ConditionalOnMissingBean(ApplicationEventMulticaster.class)
//	@Bean
//	public ApplicationEventMulticaster applicationEventMulticaster() {
//		SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
//		eventMulticaster.setTaskExecutor(taskThreadPool());
//		eventMulticaster.setErrorHandler(new DefaultErrorHandler());
//		return eventMulticaster;
//	}
//
//	@Slf4j
//	public static class DefaultErrorHandler implements ErrorHandler {
//
//		@Override
//		public void handleError(Throwable e) {
//			log.error(e.getMessage(), e);
//		}
//
//	}

}
