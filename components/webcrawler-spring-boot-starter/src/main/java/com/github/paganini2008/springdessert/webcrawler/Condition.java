package com.github.paganini2008.springdessert.webcrawler;

import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * Condition
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public interface Condition {

	void reset(long catalogId);

	boolean mightComplete(long catalogId, Tuple tuple);

	boolean isCompleted(long catalogId, Tuple tuple);

}
