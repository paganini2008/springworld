package com.github.paganini2008.springdessert.xtransport;

import com.github.paganini2008.xtransport.Tuple;

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
