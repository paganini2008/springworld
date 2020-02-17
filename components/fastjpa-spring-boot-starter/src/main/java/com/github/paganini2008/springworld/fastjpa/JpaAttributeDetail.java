package com.github.paganini2008.springworld.fastjpa;

/**
 * 
 * JpaAttributeDetail
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface JpaAttributeDetail {

	String getName();

	Class<?> getJavaType();

	boolean isId();

	boolean isVersion();

	boolean isOptional();

	boolean isAssociation();

	boolean isCollection();

}
