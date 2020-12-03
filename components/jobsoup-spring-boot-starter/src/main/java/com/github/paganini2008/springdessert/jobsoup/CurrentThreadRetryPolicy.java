package com.github.paganini2008.springdessert.jobsoup;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * CurrentThreadRetryPolicy
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class CurrentThreadRetryPolicy implements RetryPolicy {

	@Qualifier(BeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Override
	public Object retryIfNecessary(JobKey jobKey, Job job, Object attachment, Throwable reason, int retries, Logger log) throws Throwable {
		jobExecutor.execute(job, attachment, retries);
		throw reason;
	}

}
