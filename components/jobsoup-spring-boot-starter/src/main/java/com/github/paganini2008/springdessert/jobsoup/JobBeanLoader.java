package com.github.paganini2008.springdessert.jobsoup;

/**
 * 
 * JobBeanLoader
 *
 * @author Jimmy Hoff
 */
public interface JobBeanLoader {

	Job loadJobBean(JobKey jobKey) throws Exception;

}
