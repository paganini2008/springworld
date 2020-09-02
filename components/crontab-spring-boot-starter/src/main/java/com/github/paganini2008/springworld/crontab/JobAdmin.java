package com.github.paganini2008.springworld.crontab;

/**
 * 
 * JobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobAdmin {

	JobState persistJob(JobConfig jobConfig) throws Exception;

	JobState deleteJob(JobKey jobKey) throws Exception;

	JobState hasJob(JobKey jobKey) throws Exception;

	JobState triggerJob(JobKey jobKey, Object attachment) throws Exception;

}