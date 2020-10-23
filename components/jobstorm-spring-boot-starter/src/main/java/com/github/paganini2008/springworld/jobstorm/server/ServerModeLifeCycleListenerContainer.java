package com.github.paganini2008.springworld.jobstorm.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.jobstorm.JobAdmin;
import com.github.paganini2008.springworld.jobstorm.JobKey;
import com.github.paganini2008.springworld.jobstorm.JobLifeCycle;
import com.github.paganini2008.springworld.jobstorm.LifeCycleListenerContainer;

/**
 * 
 * ServerModeLifeCycleListenerContainer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ServerModeLifeCycleListenerContainer extends LifeCycleListenerContainer {
	
	@Autowired
	private JobAdmin jobAdmin;

	@Override
	public void onChange(JobKey jobKey, JobLifeCycle lifeCycle) {
		jobAdmin.publicLifeCycleEvent(jobKey, lifeCycle);
	}

}
