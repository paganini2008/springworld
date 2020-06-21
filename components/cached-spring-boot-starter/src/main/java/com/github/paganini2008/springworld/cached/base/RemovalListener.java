package com.github.paganini2008.springworld.cached.base;

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
