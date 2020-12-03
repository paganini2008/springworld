package com.github.paganini2008.xtransport;

import java.net.SocketAddress;

/**
 * 
 * NioConnection
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface NioConnection {

	void connect(SocketAddress remoteAddress, HandshakeCallback handshakeCallback);

	boolean isConnected(SocketAddress remoteAddress);

}
