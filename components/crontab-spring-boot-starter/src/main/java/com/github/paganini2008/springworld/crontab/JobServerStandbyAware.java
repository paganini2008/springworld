package com.github.paganini2008.springworld.crontab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.github.paganini2008.springworld.cluster.ApplicationClusterLeaderStandbyEvent;

/**
 * 
 * JobServerStandbyAware
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Component
public class JobServerStandbyAware implements ApplicationListener<ApplicationClusterLeaderStandbyEvent> {

	@Autowired
	private JobManager jobManager;

	@Override
	public void onApplicationEvent(ApplicationClusterLeaderStandbyEvent event) {
		if (jobManager instanceof PersistentJobsInitializer) {
			((PersistentJobsInitializer) jobManager).reloadPersistentJobs();
		}
		jobManager.runNow();
	}

}
