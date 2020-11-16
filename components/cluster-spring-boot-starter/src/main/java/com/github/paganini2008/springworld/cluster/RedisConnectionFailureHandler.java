package com.github.paganini2008.springworld.cluster;

import org.springframework.beans.factory.annotation.Autowired;
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
public class RedisConnectionFailureHandler implements ConnectionFailureHandler {

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private RedisKeepAliveResolver redisKeepAliveResolver;

	@Autowired
	private RetryTemplateFactory retryTemplateFactory;

	@Override
	public void handleException(Throwable e) {
		final RetryTemplate retryTemplate = retryTemplateFactory.setRetryPolicy(3).createObject();
		retryTemplate.execute(context -> {
			return redisKeepAliveResolver.ping();
		}, context -> {
			Throwable reason = context.getLastThrowable();
			if (reason != null) {
				log.error(reason.getMessage(), reason);
			}
			instanceId.setClusterState(ClusterState.FATAL);
			redisKeepAliveResolver.addListener(RedisConnectionFailureHandler.this);
			return "";
		});
	}

}
