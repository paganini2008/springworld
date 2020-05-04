package com.github.paganini2008.embeddedio;

/**
 * 
 * ChannelEventPublisher
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface ChannelEventPublisher {

	void publishChannelEvent(ChannelEvent event);

	void subscribeChannelEvent(ChannelHandler channelHandler);

	void subscribeChannelEvent(ChannelEventListener listener);

	void destroy();
}
