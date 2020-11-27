package com.github.paganini2008.springdessert.webcrawler;

import com.github.paganini2008.transport.Tuple;

/**
 * 
 * FinishableCondition
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface FinishableCondition {

	boolean couldFinish(Tuple tuple);

}
