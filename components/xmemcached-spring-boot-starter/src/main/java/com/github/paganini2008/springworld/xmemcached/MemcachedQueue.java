package com.github.paganini2008.springworld.xmemcached;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.paganini2008.devtools.collection.MapUtils;

import lombok.Getter;
import net.rubyeye.xmemcached.Counter;

/**
 * 
 * MemcachedQueue
 *
 * @author Fred Feng
 * @version 1.0
 */
public final class MemcachedQueue {

	private static final String PUSHING_KEY = ":pushing";
	private static final String POPPING_KEY = ":popping";

	private final MemcachedOperations operations;
	private final Map<String, QueueCounter> counters = new ConcurrentHashMap<String, QueueCounter>();

	public MemcachedQueue(MemcachedOperations operations) {
		this.operations = operations;
	}

	public boolean push(String key, int expiration, Object value) throws Exception {
		QueueCounter counter = MapUtils.get(counters, key, () -> {
			return new QueueCounter(key);
		});
		Counter pushing = counter.getPushing();
		Counter popping = counter.getPopping();
		if (popping.get() > pushing.get()) {
			pushing.set(popping.get());
		}
		String serialKey = key + ":" + pushing.incrementAndGet();
		return operations.set(serialKey, expiration, value);
	}

	public <T> T pop(String key, Class<T> requiredType) throws Exception {
		QueueCounter counter = MapUtils.get(counters, key, () -> {
			return new QueueCounter(key);
		});
		Counter pushing = counter.getPushing();
		Counter popping = counter.getPopping();
		if (pushing.get() <= popping.get()) {
			return null;
		}
		String serialKey = key + ":" + popping.incrementAndGet();
		T value;
		if ((value = operations.get(serialKey, requiredType)) != null) {
			operations.getClient().deleteWithNoReply(serialKey);
			return value;
		}
		return null;
	}

	@Getter
	class QueueCounter {

		private Counter pushing;
		private Counter popping;

		QueueCounter(String key) {
			pushing = operations.getClient().getCounter(key + PUSHING_KEY, 0);
			popping = operations.getClient().getCounter(key + POPPING_KEY, 0);
		}

	}

}
