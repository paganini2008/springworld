package com.github.paganini2008.springworld.jobstorm.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.jobstorm.JobAdmin;
import com.github.paganini2008.springworld.jobstorm.JobKey;
import com.github.paganini2008.springworld.jobstorm.JobLifeCycle;
import com.github.paganini2008.springworld.jobstorm.LifeCycleListenerContainer;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ServerModeLifeCycleListenerContainer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ServerModeLifeCycleListenerContainer extends LifeCycleListenerContainer {

	@Autowired
	private JobAdmin jobAdmin;

	@Override
	public void onChange(JobKey jobKey, JobLifeCycle lifeCycle) {
		try {
			jobAdmin.publicLifeCycleEvent(jobKey, lifeCycle);
		} catch (NoJobResourceException e) {
			log.warn("Job: " + jobKey.toString() + " is not available now.");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
