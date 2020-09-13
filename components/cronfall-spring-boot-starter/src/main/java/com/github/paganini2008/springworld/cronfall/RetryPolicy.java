package com.github.paganini2008.springworld.cronfall;

import org.slf4j.Logger;

/**
 * 
 * RetryPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface RetryPolicy {

	Object retryIfNecessary(JobKey jobKey, Job job, Object attachment, Throwable reason, int retries, Logger log) throws Throwable;

}
