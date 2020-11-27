package com.github.paganini2008.springdessert.cached;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.scheduling.annotation.Async;

import com.github.paganini2008.springdessert.cluster.consistency.ConsistencyRequestContext;

/**
 * 
 * OperationNotificationEventListener
 *
 * @author Fred Feng
 * @since 1.0
 */
public class OperationNotificationEventListener implements ApplicationContextAware, SmartApplicationListener {

	@Value("${spring.application.cluster.cache.concurrents:8}")
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
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return eventType == OperationNotificationEvent.class;
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return sourceType == ApplicationClusterCache.class;
	}

	@Async
	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		final OperationNotificationEvent event = (OperationNotificationEvent) applicationEvent;
		final OperationNotification operationNotification = event.getOperationNotification();
		final String key = operationNotification.getKey();
		if (context.propose(key, operationNotification, timeout)) {
			eventProcessor.watch(key, argument -> {
				lock.release();
				applicationContext.publishEvent(new OperationSynchronizationEvent(argument));
				OperationNotification next = pendingQueue.poll();
				if (next != null) {
					applicationContext.publishEvent(new OperationNotificationEvent(event.getSource(), next));
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
