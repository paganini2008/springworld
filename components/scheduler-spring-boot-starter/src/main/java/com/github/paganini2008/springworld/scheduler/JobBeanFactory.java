package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobBeanFactory
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobBeanFactory {

	Job newJobBean(JobConfig jobConfig);

}
