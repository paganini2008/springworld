package com.github.paganini2008.springdessert.jobsoup;

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
