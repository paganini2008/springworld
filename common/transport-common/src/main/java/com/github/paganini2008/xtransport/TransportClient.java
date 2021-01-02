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

	default void send(CharSequence json) {
		send(Tuple.byString(json.toString()));
	}

	void send(Tuple tuple);
	
	boolean isActive();
	
	default void close() {
	}

}