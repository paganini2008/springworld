package com.github.paganini2008.xtransport;

/**
 * 
 * TransportClient
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface TransportClient {
	
	String[] getChannels();

	void send(CharSequence json);

	void send(Tuple tuple);

	boolean isStarted();

	void start();

	void close();

}