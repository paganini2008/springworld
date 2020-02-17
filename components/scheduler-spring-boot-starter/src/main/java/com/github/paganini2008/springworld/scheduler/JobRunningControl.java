package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobRunningControl
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface JobRunningControl {

	boolean isRunning();

	void pause();

	void resume();

}
