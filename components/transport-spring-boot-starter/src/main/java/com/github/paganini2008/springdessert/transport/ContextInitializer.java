package com.github.paganini2008.springdessert.transport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springdessert.cluster.ApplicationClusterLeaderEvent;

/**
 * 
 * ContextInitializer
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class ContextInitializer implements ApplicationListener<ApplicationClusterLeaderEvent> {

	@Autowired
	private Counter counter;

	@Override
	public void onApplicationEvent(ApplicationClusterLeaderEvent event) {
		counter.reset();
	}

}
