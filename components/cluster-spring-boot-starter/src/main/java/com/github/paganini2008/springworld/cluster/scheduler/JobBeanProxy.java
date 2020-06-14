package com.github.paganini2008.springworld.cluster.scheduler;

/**
 * 
 * JobBeanProxy
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobBeanProxy {

	boolean isRunning();

	void pause();

	void resume();

}
