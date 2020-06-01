package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.redisplus.common.SharedLatch;

/**
 * 
 * ProcessPoolExecutor
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
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
	private PendingQueue pendingQueue;

	private final AtomicBoolean running = new AtomicBoolean(true);

	@Override
	public void submit(String beanName, Class<?> beanClass, String methodName, Object... arguments) {
		if (!running.get()) {
			throw new IllegalStateException("ProcessPool is shutdown now.");
		}
		SignatureInfo signature = new SignatureInfo(beanName, beanClass.getName(), methodName);
		if (arguments != null) {
			signature.setArguments(arguments);
		}
		boolean acquired = timeout > 0 ? sharedLatch.acquire(timeout, TimeUnit.SECONDS) : sharedLatch.acquire();
		if (acquired) {
			String topic = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + ":" + applicationName + ":process-pool-task";
			clusterMulticastGroup.unicast(topic, signature);
		} else {
			pendingQueue.set(signature);
		}

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
