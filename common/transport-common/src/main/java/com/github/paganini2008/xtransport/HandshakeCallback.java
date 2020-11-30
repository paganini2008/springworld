package com.github.paganini2008.xtransport;

import java.net.SocketAddress;

/**
 * 
 * HandshakeCallback
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@FunctionalInterface
public interface HandshakeCallback {

	void operationComplete(SocketAddress address);

}
