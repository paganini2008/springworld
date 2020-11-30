package com.github.paganini2008.springdessert.xtransport;

import java.util.List;

import com.github.paganini2008.xtransport.Tuple;

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
