package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.RejectedExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.redisplus.BeanNames;

/**
 * 
 * RedisPendingQueue
 *
 * @author Fred Feng
 * @version 1.0
 */
public class RedisPendingQueue implements PendingQueue {

	@Autowired
	@Qualifier(BeanNames.REDIS_TEMPLATE)
	private RedisTemplate<String, Object> redisTemplate;

	@Value("${spring.application.cluster.pool.queueSize:-1}")
	private int queueMaxSize;

	@Value("${spring.application.name}")
	private String applicationName;

	public void set(Signature signature) {
		String key = getKey();
		long queueSize = redisTemplate.opsForList().size(key);
		if (queueMaxSize == -1 || queueSize <= queueMaxSize) {
			redisTemplate.opsForList().leftPush(key, signature);
		} else {
			throw new RejectedExecutionException("Pool pending queue has been full. Current size is " + queueSize);
		}
	}

	public Signature get() {
		return (Signature) redisTemplate.opsForList().leftPop(getKey());
	}

	public void waitForTermination() {
		while (redisTemplate.opsForList().size(getKey()) != 0) {
			ThreadUtils.randomSleep(1000L);
		}
	}

	public int size() {
		Number result = redisTemplate.opsForList().size(getKey());
		return result != null ? result.intValue() : 0;
	}

	private String getKey() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + ":" + applicationName + ":pending-queue";
	}

}
