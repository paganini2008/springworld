package com.github.paganini2008.springdessert.webcrawler;

import com.github.paganini2008.transport.Tuple;

/**
 * 
 * FinishCondition
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface FinishCondition {

	boolean couldFinish(Tuple tuple);

}
