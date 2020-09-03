package com.github.paganini2008.springworld.fastjpa;

/**
 * 
 * JpaResultSet
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JpaResultSet<E> extends JpaPageQuery<E> {

	<T> T getResult(Class<T> requiredType);

	<T> JpaPageQuery<T> setTransformer(Transformer<E, T> transformer);

}
