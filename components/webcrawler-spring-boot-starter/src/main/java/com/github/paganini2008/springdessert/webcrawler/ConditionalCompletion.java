package com.github.paganini2008.springdessert.webcrawler;

import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * ConditionalCompletion
 *
 * @author Fred Feng
 * 
 * @since 1.0
 */
public interface ConditionalCompletion {

	void reset(Long catalogId);

	boolean mightComplete(Tuple tuple);

	boolean isCompleted(Tuple tuple);

}
