package com.github.paganini2008.springdessert.jobsoup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastGroup;
import com.github.paganini2008.springdessert.jobsoup.model.JobLifeCycleParam;

/**
 * 
 * EmbeddedModeLifeCycleListenerContainer
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class EmbeddedModeLifeCycleListenerContainer extends LifeCycleListenerContainer {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private ApplicationMulticastGroup multicastGroup;

	@Override
	public void onChange(JobKey jobKey, JobLifeCycle lifeCycle) {
		multicastGroup.multicast(applicationName, LifeCycleListenerContainer.class.getName(),
				new JobLifeCycleParam(jobKey, lifeCycle));
	}

}
