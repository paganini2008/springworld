package com.github.paganini2008.springworld.crontab;

import com.github.paganini2008.devtools.multithreads.Executable;

/**
 * 
 * Job
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface Job extends Executable {

	String getName();

	default String getDescription() {
		return "";
	}

}
