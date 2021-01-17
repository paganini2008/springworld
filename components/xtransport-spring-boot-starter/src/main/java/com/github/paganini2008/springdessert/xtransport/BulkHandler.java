package com.github.paganini2008.springdessert.xtransport;

import java.util.List;

import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * BulkHandler
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface BulkHandler {

	void onBatch(List<Tuple> list);

}
