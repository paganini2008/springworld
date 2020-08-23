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

	JobState persistJob(JobConfig jobConfig);

	JobState deleteJob(JobKey jobKey);

	JobState hasJob(JobKey jobKey);

	JobState triggerJob(JobKey jobKey, Object attachment);

}