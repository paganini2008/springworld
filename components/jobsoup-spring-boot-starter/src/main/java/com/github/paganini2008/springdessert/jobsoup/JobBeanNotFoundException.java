package com.github.paganini2008.springdessert.jobsoup;

/**
 * 
 * JobBeanNotFoundException
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class JobBeanNotFoundException extends JobException {

	private static final long serialVersionUID = 8532159543543294967L;

	public JobBeanNotFoundException(JobKey jobKey) {
		super(jobKey.toString());
	}

}
