package com.github.paganini2008.transport;

import java.util.concurrent.TimeUnit;

/**
 * 
 * NioClient
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface NioClient extends LifeCycle, NioConnection {

	void setThreadCount(int nThreads);

	void watchConnection(int interval, TimeUnit timeUnit);

	void setIdleTimeout(int idleTime);

	void send(Object data);

	void send(Object data, Partitioner partitioner);

}
