package com.github.paganini2008.springdessert.fastjpa;

/**
 * 
 * JpaDelete
 * 
 * @author Fred Feng
 * 
 */
public interface JpaDelete<E> extends Executable{

	JpaDelete<E> filter(Filter filter);

	<X> JpaSubQuery<X, X> subQuery(Class<X> entityClass);

}
