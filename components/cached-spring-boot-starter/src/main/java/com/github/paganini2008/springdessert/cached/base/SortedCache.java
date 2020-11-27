package com.github.paganini2008.springdessert.cached.base;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 
 * SortedCache
 *
 * @author Fred Feng
 * @since 1.0
 */
public class SortedCache extends BasicCache {

	public SortedCache(int maxSize, boolean asc) {
		this(maxSize, (a, b) -> {
			return asc ? a.compareToIgnoreCase(b) : b.compareToIgnoreCase(a);
		});
	}

	public SortedCache(int maxSize, Comparator<String> c) {
		setKeyExpirationPolicy(new SortedKeyExpirationPolicy(maxSize, c));
	}

	/**
	 * 
	 * SortedKeyExpirationPolicy
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	private static class SortedKeyExpirationPolicy implements KeyExpirationPolicy {

		private final NavigableSet<String> keys;
		private final int maxSize;

		SortedKeyExpirationPolicy(int maxSize, Comparator<String> c) {
			this.keys = new ConcurrentSkipListSet<String>(c);
			this.maxSize = maxSize;
		}

		@Override
		public void onSort(String key, Cache cache) {
			keys.add(key);
			if (keys.size() > maxSize) {
				String oldestKey = keys.pollLast();
				cache.evict(oldestKey, RemovalReason.EVICTION);
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
