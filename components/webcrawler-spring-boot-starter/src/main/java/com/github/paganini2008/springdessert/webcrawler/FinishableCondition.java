package com.github.paganini2008.springdessert.webcrawler;

import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * FinishableCondition
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface FinishableCondition {

	boolean mightFinish(Tuple tuple);
	
	boolean isFinished();

}
