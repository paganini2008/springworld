package com.github.paganini2008.springworld.cache;

/**
 * 
 * RemovalListener
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface RemovalListener {

	default void onRemoval(RemovalNotification removalNotification) {
	}

}
