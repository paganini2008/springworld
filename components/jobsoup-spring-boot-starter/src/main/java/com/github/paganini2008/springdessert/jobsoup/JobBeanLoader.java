package com.github.paganini2008.springdessert.jobsoup;

/**
 * 
 * JobBeanLoader
 *
 * @author Fred Feng
 */
public interface JobBeanLoader {

	Job loadJobBean(JobKey jobKey) throws Exception;

}
