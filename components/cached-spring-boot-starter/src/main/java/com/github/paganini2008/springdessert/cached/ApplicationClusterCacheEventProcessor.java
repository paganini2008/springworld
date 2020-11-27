package com.github.paganini2008.springdessert.cached;

import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.Observer;
import com.github.paganini2008.springdessert.cached.base.Cache;
import com.github.paganini2008.springdessert.cluster.consistency.ConsistencyRequest;
import com.github.paganini2008.springdessert.cluster.consistency.ConsistencyRequestConfirmationEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationClusterCacheEventProcessor
 *
 * @author Fred Feng
 * @since 1.0
 */
@SuppressWarnings("unchecked")
@Slf4j
public class ApplicationClusterCacheEventProcessor implements SmartApplicationListener {

	private final Observable operations = Observable.unrepeatable();
	private final Observable signatures = Observable.repeatable();

	@Qualifier("cacheDelegate")
	@Autowired
	private Cache cache;

	@PostConstruct
	public void configure() {

		signatures.addObserver(MethodSignatures.parse("cache", "set"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.set(operationNotification.getKey(), operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("cache", "increment"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			operationNotification.setReturnResult(cache.increment(operationNotification.getKey()));
		});
		signatures.addObserver(MethodSignatures.parse("cache", "decrement"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			operationNotification.setReturnResult(cache.decrement(operationNotification.getKey()));
		});
		signatures.addObserver(MethodSignatures.parse("cache", "addLong"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			operationNotification.setReturnResult(cache.addLong(operationNotification.getKey(), (Long) operationNotification.getValue()));
		});
		signatures.addObserver(MethodSignatures.parse("cache", "addDouble"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			operationNotification
					.setReturnResult(cache.addDouble(operationNotification.getKey(), (Double) operationNotification.getValue()));
		});
		signatures.addObserver(MethodSignatures.parse("cache", "delete"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.delete(operationNotification.getKey());
		});
		signatures.addObserver(MethodSignatures.parse("cache", "clear"), (ob, argument) -> {
			cache.clear();
		});

		signatures.addObserver(MethodSignatures.parse("hash", "set"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.hash().set(operationNotification.getKey(), operationNotification.getName(), operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("hash", "append"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.hash().append(operationNotification.getKey(), (Map<String, Object>) operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("hash", "delete"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.hash().delete(operationNotification.getKey(), operationNotification.getName());
		});

		signatures.addObserver(MethodSignatures.parse("set", "add"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.set().add(operationNotification.getKey(), operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("set", "append"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.set().append(operationNotification.getKey(), (Collection<Object>) operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("set", "pollFirst"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			operationNotification.setReturnResult(cache.set().pollFirst(operationNotification.getKey()));
		});
		signatures.addObserver(MethodSignatures.parse("set", "pollLast"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			operationNotification.setReturnResult(cache.set().pollLast(operationNotification.getKey()));
		});

		signatures.addObserver(MethodSignatures.parse("list", "addFirst"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.list().addFirst(operationNotification.getKey(), operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("list", "addLast"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.list().addLast(operationNotification.getKey(), operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("list", "append"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.list().append(operationNotification.getKey(), (Collection<Object>) operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("list", "pollFirst"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			operationNotification.setReturnResult(cache.list().pollFirst(operationNotification.getKey()));
		});
		signatures.addObserver(MethodSignatures.parse("list", "pollLast"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			operationNotification.setReturnResult(cache.list().pollLast(operationNotification.getKey()));
		});
	}

	public void watch(String key, Watcher watcher) {
		operations.addObserver(key, new Observer() {

			@Override
			public void update(Observable ob, Object arg) {
				final OperationNotification operationNotification = (OperationNotification) arg;
				watcher.operate(operationNotification);
			}

		});
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return eventType == ConsistencyRequestConfirmationEvent.class;
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return sourceType == OperationNotification.class;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		final ConsistencyRequestConfirmationEvent event = (ConsistencyRequestConfirmationEvent) applicationEvent;
		final ConsistencyRequest result = event.getRequest();
		if (event.isOk()) {
			final String name = result.getName();
			final OperationNotification operationNotification = (OperationNotification) result.getValue();
			signatures.notifyObservers(operationNotification.getSignature(), operationNotification);
			operations.notifyObservers(name, operationNotification);
			if (log.isTraceEnabled()) {
				log.trace("ApplicationClusterCache size: " + cache.size());
			}
		}
	}

}
