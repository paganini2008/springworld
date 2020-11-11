package com.github.paganini2008.springworld.restclient;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * 
 * Feature
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Feature {

	String path();

	int retries() default 0;

	int timeout() default -1;

	HttpMethod method() default HttpMethod.GET;

	String contentType() default MediaType.APPLICATION_JSON_VALUE;

	Class<? super Throwable>[] fallbackException() default {};

	HttpStatus[] fallbackHttpStatus() default {};

}
