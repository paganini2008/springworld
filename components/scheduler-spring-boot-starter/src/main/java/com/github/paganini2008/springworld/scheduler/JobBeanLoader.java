package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobBeanLoader
 *
 * @author Fred Feng
 */
public interface JobBeanLoader {

	Job defineJob(JobParameter jobParameter) throws Exception;

}
