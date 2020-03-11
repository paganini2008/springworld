package com.github.paganini2008.transport;

import java.util.EventListener;

/**
 * 
 * ChannelEventListener
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface ChannelEventListener<T> extends EventListener {

	default void fireChannelEvent(ChannelEvent<T> channelEvent) {
	}

}
