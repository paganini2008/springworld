package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * SerialJob
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface SerialJob extends Job {
	
	String[] getDependencies();
}
