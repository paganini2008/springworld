package com.github.paganini2008.springworld.joblink;

/**
 * 
 * JobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobAdmin {

	JobState triggerJob(JobKey jobKey, Object attachment) throws Exception;

}