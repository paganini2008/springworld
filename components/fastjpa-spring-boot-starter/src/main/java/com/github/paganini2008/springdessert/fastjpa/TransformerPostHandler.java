package com.github.paganini2008.springdessert.fastjpa;

import javax.persistence.Tuple;

/**
 * 
 * TransformerPostHandler
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface TransformerPostHandler<T> {

	void handleAfterTransferring(Tuple tuple, T output);

}
