package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * SerializableJob
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface SerializableJob extends Job {

	String[] getDependencies();

}
