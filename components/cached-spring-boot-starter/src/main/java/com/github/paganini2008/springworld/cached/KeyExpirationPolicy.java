package com.github.paganini2008.springworld.cached;

/**
 * 
 * KeyExpirationPolicy
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface KeyExpirationPolicy {

	default void onOrder(String key, Cache cache) {
	}

	default void onDelete(String key, Cache cache) {
	}

	default void onClear(Cache cache) {
	}

}
