package com.github.paganini2008.springworld.fastjpa;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

/**
 * 
 * JpaResultSet
 * 
 * @author Fred Feng
 * 
 * 
 */
public interface JpaResultSet<E> extends ResultSetSlice<E> {

	<T> T getResult(Class<T> requiredType);

	<T> ResultSetSlice<T> setTransformer(Transformer<E, T> transformer);

}
