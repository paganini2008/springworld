package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * JobService
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobService {

	@Autowired
	private JobManager jobManager;
	
	@Autowired
	private ScheduleManager scheduleManager;
	
}
