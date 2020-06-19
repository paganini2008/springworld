package com.github.paganini2008.springworld.cached;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 
 * FifoCache
 *
 * @author Fred Feng
 * @since 1.0
 */
public class FifoCache extends BasicCache {

	public FifoCache(int maxSize) {
		setKeyExpirationPolicy(new FifoKeyExpirationPolicy(maxSize));
	}

	/**
	 * 
	 * FifoKeyExpirationPolicy
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	private static class FifoKeyExpirationPolicy implements KeyExpirationPolicy {

		private final Queue<String> keys;
		private final int maxSize;

		FifoKeyExpirationPolicy(int maxSize) {
			this.keys = new ConcurrentLinkedQueue<String>();
			this.maxSize = maxSize;
		}

		@Override
		public void onOrder(String key, Cache cache) {
			if (!keys.contains(key)) {
				keys.add(key);
				if (keys.size() > maxSize) {
					String oldestKey = keys.poll();
					cache.evict(oldestKey, RemovalReason.EVICTION);
				}
			}
		}

		@Override
		public void onDelete(String key, Cache cache) {
			keys.remove(key);
		}

		@Override
		public void onClear(Cache cache) {
			keys.clear();
		}

	}

}
