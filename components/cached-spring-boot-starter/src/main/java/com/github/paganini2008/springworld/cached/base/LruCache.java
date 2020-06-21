package com.github.paganini2008.springworld.cached.base;

import com.github.paganini2008.devtools.collection.LruList;

/**
 * 
 * LruCache
 *
 * @author Fred Feng
 * @since 1.0
 */
public class LruCache extends BasicCache {

	public LruCache(int maxSize) {
		this.setKeyExpirationPolicy(new LruKeyExpirationPolicy(this, maxSize));
	}

	/**
	 * 
	 * LruKeyExpirationPolicy
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	private static class LruKeyExpirationPolicy implements KeyExpirationPolicy {

		private final LruList<String> keys;

		LruKeyExpirationPolicy(final Cache delegate, int maxSize) {
			this.keys = new LruList<String>(maxSize) {

				private static final long serialVersionUID = 1L;

				public void onEviction(String oldestKey) {
					delegate.evict(oldestKey, RemovalReason.EVICTION);
				}
			};
		}

		@Override
		public void onOrder(String key, Cache cache) {
			if (!keys.contains(key)) {
				keys.add(key);
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
