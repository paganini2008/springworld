package com.github.paganini2008.springdessert.fastjpa.support;

import com.github.paganini2008.devtools.collection.Tuple;

/**
 * 
 * RowMapper
 * 
 * @author Fred Feng
 * 
 */
public interface RowMapper<T> {

	T mapRow(Tuple tuple);

}
