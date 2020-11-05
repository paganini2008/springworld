package com.github.paganini2008.springworld.fastjpa;

import javax.persistence.Tuple;

/**
 * 
 * TransformerPostHandler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface TransformerPostHandler<T> {

	void handleAfterTransferring(Tuple tuple, T output);

}
