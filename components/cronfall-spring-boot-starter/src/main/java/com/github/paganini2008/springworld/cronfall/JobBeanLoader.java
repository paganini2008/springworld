package com.github.paganini2008.springworld.cronfall;

/**
 * 
 * JobBeanLoader
 *
 * @author Fred Feng
 */
public interface JobBeanLoader {

	Job loadJobBean(JobKey jobKey) throws Exception;

}
