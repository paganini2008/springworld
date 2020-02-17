package com.github.paganini2008.springworld.fastjpa;

import java.util.List;

/**
 * 
 * JpaCustomQuery
 * 
 * @author Fred Feng
 * 
 * 
 */
public interface JpaCustomQuery<X> {

	default JpaQuery<X> select(Class<X> domainClass) {
		return select(domainClass, Model.ROOT);
	}

	JpaQuery<X> select(Class<X> domainClass, String alias);

	<T> T getSingleResult(JpaQueryCallback<T> callback);

	<T> List<T> getResultList(JpaQueryCallback<T> callback);

	<T> List<T> getResultList(JpaQueryCallback<T> callback, int limit, int offset);
}
