package com.github.paganini2008.embeddedio;

import java.net.SocketAddress;

/**
 * 
 * Channel
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface Channel {

	long writeAndFlush(Object message);

	long write(Object message, int batchSize);

	long flush();

	long read();

	boolean isActive();
	
	SocketAddress getLocalAddr();
	
	SocketAddress getRemoteAddr();
	
	void close();

}