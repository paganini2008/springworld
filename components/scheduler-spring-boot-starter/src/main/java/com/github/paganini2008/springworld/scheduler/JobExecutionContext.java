package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobExecutionContext
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface JobExecutionContext {

	void writeVar(String name, Object value);

	Object readVar(String name);

	JobRuntime getJobRuntime(Job job) throws Exception;

	void pause(Job job) throws Exception;

	void resume(Job job) throws Exception;

	Object getAttachment(Job job) throws Exception;

}
