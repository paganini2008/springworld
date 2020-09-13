package com.github.paganini2008.springworld.cronfall;

/**
 * 
 * RetryPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface RetryPolicy {

	Object retryIfNecessary(JobKey jobKey, Job job, Object attachment, Throwable reason, int retries) throws Throwable;

}
