package com.github.paganini2008.springdessert.fastjpa;

import java.util.List;

/**
 * 
 * JpaCustomQuery
 *
 * @author Jimmy Hoff
 * @version 1.0
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
