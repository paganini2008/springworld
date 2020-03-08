package com.github.paganini2008.springworld.jdbc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.paganini2008.devtools.collection.Tuple;

/**
 * 
 * Slice
 *
 * @author Fred Feng
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Slice {

	Class<?> pageableSql() default Void.class;
	
	String value();

	Class<?> elementType() default Tuple.class;

	boolean javaType() default false;

}
