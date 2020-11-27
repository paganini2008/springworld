package com.github.paganini2008.springdessert.fastjpa;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

/**
 * 
 * JpaResultSet
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JpaResultSet<E> extends ResultSetSlice<E> {

	<T> T getResult(Class<T> requiredType);

	<T> ResultSetSlice<T> setTransformer(Transformer<E, T> transformer);

}
