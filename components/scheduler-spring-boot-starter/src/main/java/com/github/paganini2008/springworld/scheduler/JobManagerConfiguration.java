package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.github.paganini2008.springworld.cluster.ApplicationClusterNewLeaderEvent;

/**
 * 
 * JobManagerConfiguration
 *
 * @author Fred Feng
 * @version 1.0
 */
@Component
public class JobManagerConfiguration implements ApplicationListener<ApplicationClusterNewLeaderEvent> {

	@Autowired
	private JobManager jobManager;

	@Override
	public void onApplicationEvent(ApplicationClusterNewLeaderEvent event) {
		jobManager.doSchedule();
	}

}
