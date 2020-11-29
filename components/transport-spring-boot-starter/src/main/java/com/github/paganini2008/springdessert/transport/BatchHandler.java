package com.github.paganini2008.springdessert.transport;

import java.util.List;

import com.github.paganini2008.transport.Tuple;

/**
 * 
 * BatchHandler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface BatchHandler {

	void onBatch(List<Tuple> list);

}
