package com.github.paganini2008.springworld.transport.transport;

import com.github.paganini2008.transport.NioClient;

/**
 * 
 * Transport
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface Transport {

	NioServer getNioServer();
	
	NioClient getNioClient();

}
