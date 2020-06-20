package com.github.paganini2008.springworld.cached;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springworld.cluster.consistency.ConsistencyRequestContext;

/**
 * 
 * OperationNotificationEventListener
 *
 * @author Fred Feng
 * @since 1.0
 */
public class OperationNotificationEventListener implements ApplicationContextAware, ApplicationListener<OperationNotificationEvent> {

	@Value("${spring.application.cluster.cache.concurrents:1}")
	private int concurrents;

	@Value("${spring.application.cluster.cache.executionTimeout:60}")
	private int timeout;

	@Autowired
	private ConsistencyRequestContext context;

	@Autowired
	private ApplicationClusterCacheEventProcessor eventProcessor;

	private Semaphore lock;

	private final Queue<OperationNotification> pendingQueue = new ConcurrentLinkedQueue<OperationNotification>();

	@PostConstruct
	public void configure() {
		this.lock = new Semaphore(concurrents, true);
	}

	@Override
	public void onApplicationEvent(OperationNotificationEvent event) {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		final OperationNotification operationNotification = event.getOperationNotification();
		String key = operationNotification.getKey();
		if (context.propose(key, operationNotification, timeout)) {
			eventProcessor.watch(key, argument -> {
				lock.release();
				final Cache delegate = (Cache) event.getSource();
				applicationContext.publishEvent(new OperationNotificationFinishedEvent(delegate, argument));

				OperationNotification next = pendingQueue.poll();
				if (next != null) {
					applicationContext.publishEvent(new OperationNotificationEvent(delegate, next));
				}
			});
		} else {
			pendingQueue.add(operationNotification);
			lock.release();
		}
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
