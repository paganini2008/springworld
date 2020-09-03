package com.github.paganini2008.springworld.fastjpa;

import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

/**
 * 
 * JpaPageQuery
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JpaPageQuery<E> extends ResultSetSlice<E> {

	ResultSetSlice<E> setTotalRecords(int totalRecords);

}
