package com.github.paganini2008.springworld.tx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.paganini2008.devtools.jdbc.TransactionIsolationLevel;

/**
 * 
 * Transactional
 *
 * @author Fred Feng
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Transactional {

	Class<? extends Throwable>[] rollbackFor() default {};

	int timeout() default 10;

	TransactionIsolationLevel transactionIsolationLevel() default TransactionIsolationLevel.NONE;
	
}
