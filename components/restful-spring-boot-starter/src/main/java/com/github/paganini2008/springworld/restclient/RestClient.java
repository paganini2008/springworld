package com.github.paganini2008.springworld.restclient;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * RestClient
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestClient {

	String provider() default "";

	int retries() default 0;

	int timeout() default -1;

	Class<?> fallback();

}
