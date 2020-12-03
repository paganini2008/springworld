package com.github.paganini2008.embeddedio;

import java.net.SocketAddress;

/**
 * 
 * Channel
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public interface Channel {

	long writeAndFlush(Object message);

	long write(Object message);

	long flush();

	long read();

	boolean isActive();
	
	SocketAddress getLocalAddr();
	
	SocketAddress getRemoteAddr();
	
	void close();

}