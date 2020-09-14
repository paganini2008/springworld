package com.github.paganini2008.springworld.cronkeeper;

import com.github.paganini2008.springworld.cronkeeper.model.JobPersistParam;

/**
 * 
 * JobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobAdmin {

	JobState persistJob(JobPersistParam param) throws Exception;

	JobState deleteJob(JobKey jobKey) throws Exception;

	JobState hasJob(JobKey jobKey) throws Exception;

	JobState triggerJob(JobKey jobKey, Object attachment) throws Exception;

}