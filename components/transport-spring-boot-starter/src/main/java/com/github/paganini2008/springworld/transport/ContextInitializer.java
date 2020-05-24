package com.github.paganini2008.springworld.transport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springworld.cluster.ApplicationClusterLeaderStandbyEvent;

/**
 * 
 * ContextInitializer
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class ContextInitializer implements ApplicationListener<ApplicationClusterLeaderStandbyEvent> {

	@Autowired
	private Counter counter;

	@Override
	public void onApplicationEvent(ApplicationClusterLeaderStandbyEvent event) {
		counter.reset();
	}

}
