package com.github.paganini2008.springdessert.webcrawler;

/**
 * 
 * RedisKeyPrefixes
 *
 * @author Fred Feng
 * 
 * @since 1.0
 */
public abstract class RedisKeyPrefixes {

	public final static String ID = "spring:application:cluster:%s:id:";

	public final static String COUNT = "spring:application:cluster:%s:counter:";

	public final static String DEADLINE = "spring:application:cluster:%s:deadline:";

}
