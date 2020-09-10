package com.github.paganini2008.springworld.crontab;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CurrentThreadRetryPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class CurrentThreadRetryPolicy implements RetryPolicy {

	@Override
	public Object retryIfNecessary(JobKey jobKey, Job job, Object attachment, Throwable previous, int retries) throws Throwable {
		Throwable reason = null;
		for (int i = 0; i < job.getRetries(); i++) {
			try {
				return job.execute(jobKey, attachment);
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				reason = e;
			}
		}
		throw reason;
	}

}
