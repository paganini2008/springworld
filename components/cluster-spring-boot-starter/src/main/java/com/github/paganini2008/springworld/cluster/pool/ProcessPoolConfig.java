package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastConfig;
import com.github.paganini2008.springworld.reditools.common.RedisCounter;
import com.github.paganini2008.springworld.reditools.common.RedisSharedLatch;
import com.github.paganini2008.springworld.reditools.common.SharedLatch;
import com.github.paganini2008.springworld.reditools.common.TtlKeeper;

/**
 * 
 * ProcessPoolConfig
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Configuration
@ConditionalOnBean(ClusterMulticastConfig.class)
@ConditionalOnProperty(value = "spring.application.cluster.pool.enabled", havingValue = "true")
public class ProcessPoolConfig {

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Value("${spring.application.cluster.pool.size:8}")
	private int poolSize;

	@Bean
	public RedisCounter redisCounter(RedisConnectionFactory redisConnectionFactory, TtlKeeper ttlKeeper) {
		final String fullName = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":pool";
		RedisCounter redisCounter = new RedisCounter(fullName, redisConnectionFactory);
		redisCounter.keep(ttlKeeper, 5, TimeUnit.SECONDS);
		return redisCounter;
	}

	@ConditionalOnMissingBean(SharedLatch.class)
	@Bean
	public SharedLatch sharedLatch(RedisCounter redisCounter) {
		return new RedisSharedLatch(redisCounter, poolSize);
	}

	@ConditionalOnMissingBean(PendingQueue.class)
	@Bean
	public PendingQueue pendingQueue() {
		return new RedisPendingQueue();
	}

	@Bean
	public ProcessPool processPool() {
		return new ProcessPoolExecutor();
	}

	@Bean
	public ProcessPoolTaskListener processPoolTaskListener() {
		return new ProcessPoolTaskListener();
	}

	@Bean
	public MultiProcessingInterpreter multiProcessingInterpreter() {
		return new MultiProcessingInterpreter();
	}

	@Bean
	public MultiProcessingCallbackListener multiProcessingCallbackListener() {
		return new MultiProcessingCallbackListener();
	}

	@Bean
	public MultiProcessingCompletionListener multiProcessingCompletionListener() {
		return new MultiProcessingCompletionListener();
	}

	@Bean
	public InvocationBarrier invocationBarrier() {
		return new InvocationBarrier();
	}

}
