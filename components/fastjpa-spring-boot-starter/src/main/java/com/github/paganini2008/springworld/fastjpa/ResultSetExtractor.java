package com.github.paganini2008.springworld.fastjpa;

import javax.persistence.Query;

/**
 * 
 * ResultSetExtractor
 * 
 * @author Fred Feng
 * 
 */
public interface ResultSetExtractor<T> {

	T extractData(Query query);

}
