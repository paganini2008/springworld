package com.github.paganini2008.embeddedio;

import java.io.IOException;

/**
 * 
 * ChannelHandler
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface ChannelHandler {

	default void fireChannelActive(Channel channel) throws IOException {
	}

	default void fireChannelInactive(Channel channel) throws IOException {
	}

	default void fireChannelReadable(Channel channel, MessagePacket packet) throws Exception {
	}
	
	default void fireChannelWriteable(Channel channel, MessagePacket packet) throws Exception {
	}

	default void fireChannelFatal(Channel channel, Throwable e) {
	}

}
