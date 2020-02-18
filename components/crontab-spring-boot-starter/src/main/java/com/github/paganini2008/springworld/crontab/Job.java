package com.github.paganini2008.springworld.crontab;

import com.github.paganini2008.devtools.scheduler.Task;

/**
 * 
 * Job
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface Job extends Task {

	String getName();

	default String getDescription() {
		return "";
	}

}
