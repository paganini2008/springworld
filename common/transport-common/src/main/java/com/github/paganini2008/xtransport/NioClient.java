package com.github.paganini2008.xtransport;

import java.util.concurrent.TimeUnit;

/**
 * 
 * NioClient
 * 
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface NioClient extends LifeCycle, NioConnection, Client {

	void setThreadCount(int threadCount);

	void watchConnection(int checkInterval, TimeUnit timeUnit);

	void setIdleTimeout(int idleTimeout);

}
