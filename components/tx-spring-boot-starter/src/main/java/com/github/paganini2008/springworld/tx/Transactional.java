package com.github.paganini2008.springworld.tx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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

	TransactionPhase subscribeEvent() default TransactionPhase.BEFORE_CLOSE;

	String eventHandler() default "";

	int timeout() default 10;

}
