package com.github.paganini2008.springworld.transport;

import com.github.paganini2008.transport.Tuple;

/**
 * 
 * Handler
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface Handler {

	void onData(Tuple tuple);

	default String getTopic() {
		return Tuple.DEFAULT_TOPIC;
	}

}
