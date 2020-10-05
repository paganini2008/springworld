package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.reditools.common.SharedLatch;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ProcessPoolExecutor
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Slf4j
public class ProcessPoolExecutor implements ProcessPool {

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.application.cluster.pool.latch.timeout:-1}")
	private int timeout;

	@Autowired
	private SharedLatch sharedLatch;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private PendingQueue pendingQueue;

	private final AtomicBoolean running = new AtomicBoolean(true);

	@Override
	public void execute(String beanName, Class<?> beanClass, String methodName, Object... arguments) {
		checkIfRunning();
		Signature signature = new Call(beanName, beanClass.getName(), methodName, arguments);
		boolean acquired = timeout > 0 ? sharedLatch.acquire(timeout, TimeUnit.SECONDS) : sharedLatch.acquire();
		if (acquired) {
			if (log.isTraceEnabled()) {
				log.trace("Now processPool's concurrency is " + sharedLatch.cons());
			}
			clusterMulticastGroup.unicast(applicationName, ProcessPoolTaskListener.class.getName(), signature);
		} else {
			pendingQueue.add(signature);
		}

	}

	@Override
	public TaskPromise submit(String beanName, Class<?> beanClass, String methodName, Object... arguments) {
		checkIfRunning();
		Signature signature = new Call(beanName, beanClass.getName(), methodName, arguments);
		boolean acquired = timeout > 0 ? sharedLatch.acquire(timeout, TimeUnit.SECONDS) : sharedLatch.acquire();
		if (acquired) {
			if (log.isTraceEnabled()) {
				log.trace("Now processPool's concurrency is " + sharedLatch.cons());
			}
			clusterMulticastGroup.unicast(applicationName, ProcessPoolTaskListener.class.getName(), signature);
		} else {
			pendingQueue.add(signature);
		}
		ProcessPoolTaskPromise promise = new ProcessPoolTaskPromise(signature.getId());
		redisMessageSender.subscribeChannel(signature.getId(), promise);
		return promise;
	}

	private void checkIfRunning() {
		if (!running.get()) {
			throw new IllegalStateException("ProcessPool is shutdown now.");
		}
	}
	
	

	@Override
	public int getQueueSize() {
		return pendingQueue.size();
	}

	@Override
	public void shutdown() {
		if (!running.get()) {
			return;
		}
		running.set(false);
		pendingQueue.waitForTermination();
		sharedLatch.join();
	}

}
