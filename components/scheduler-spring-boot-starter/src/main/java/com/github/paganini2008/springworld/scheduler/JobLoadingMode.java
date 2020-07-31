package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobLoadingMode
 *
 * @author Fred Feng
 */
public interface JobLoadingMode {

	Job defineJob(JobDefinition jobDefinition);

}
