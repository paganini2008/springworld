package com.github.paganini2008.springdessert.tx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * XaTransactional
 *
 * @author Fred Feng
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XaTransactional {

	Class<? extends Throwable>[] rollbackFor() default Throwable.class;

	TransactionPhase subscribeEvent() default TransactionPhase.BEFORE_CLOSE;

	String eventHandler() default "";

	long timeout() default -1L;

}
