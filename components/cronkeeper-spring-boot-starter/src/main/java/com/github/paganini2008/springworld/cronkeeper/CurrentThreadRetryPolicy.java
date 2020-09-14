package com.github.paganini2008.springworld.cronkeeper;

import org.slf4j.Logger;

/**
 * 
 * CurrentThreadRetryPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class CurrentThreadRetryPolicy implements RetryPolicy {

	@Override
	public Object retryIfNecessary(JobKey jobKey, Job job, Object attachment, Throwable previous, int retries, Logger log)
			throws Throwable {
		Throwable reason = null;
		for (int i = 0; i < job.getRetries(); i++) {
			try {
				return job.execute(jobKey, attachment, log);
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				reason = e;
			}
		}
		throw reason;
	}

}
