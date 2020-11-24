package com.github.paganini2008.springdessert.jdbc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.paganini2008.devtools.jdbc.DefaultPageableSql;
import com.github.paganini2008.devtools.jdbc.PageableSql;

/**
 * 
 * Query
 *
 * @author Fred Feng
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {

	Class<? extends PageableSql> pageableSql() default DefaultPageableSql.class;

	String value();
	
	boolean singleColumn() default false;

}
