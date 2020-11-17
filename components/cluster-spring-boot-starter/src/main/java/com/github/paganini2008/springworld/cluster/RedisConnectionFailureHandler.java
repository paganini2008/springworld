package com.github.paganini2008.springworld.cluster;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.retry.support.RetryTemplate;

import com.github.paganini2008.springworld.cluster.utils.RetryTemplateFactory;
import com.github.paganini2008.springworld.reditools.common.ConnectionFailureHandler;
import com.github.paganini2008.springworld.reditools.common.RedisKeepAliveResolver;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RedisConnectionFailureHandler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class RedisConnectionFailureHandler implements ConnectionFailureHandler, ApplicationContextAware {

	@Autowired
	private RedisKeepAliveResolver redisKeepAliveResolver;

	@Autowired
	private RetryTemplateFactory retryTemplateFactory;

	private ApplicationContext applicationContext;

	@Override
	public void handleException(Throwable e) {
		log.warn("RedisConnection refused");
		final RetryTemplate retryTemplate = retryTemplateFactory.setRetryPolicy(3).createObject();
		retryTemplate.execute(context -> {
			return redisKeepAliveResolver.ping();
		}, context -> {
			Throwable reason = context.getLastThrowable();
			if (reason != null) {
				log.error(reason.getMessage(), reason);
			}
			redisKeepAliveResolver.addListener(RedisConnectionFailureHandler.this);
			applicationContext.publishEvent(new ApplicationClusterFatalEvent(applicationContext));
			return "";
		});
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
