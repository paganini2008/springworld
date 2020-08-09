package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobAdmin {

	void addJob(JobParam jobParam);

	void deleteJob(JobKey jobKey);

	boolean hasJob(JobKey jobKey);

}