package com.github.paganini2008.springdessert.cached.base;

/**
 * 
 * RemovalListener
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public interface RemovalListener {

	default void onRemoval(RemovalNotification removalNotification) {
	}

}
