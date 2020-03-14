package com.github.paganini2008.springworld.transport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springworld.cluster.ContextMasterStandbyEvent;

/**
 * 
 * ContextInitializer
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class ContextInitializer implements ApplicationListener<ContextMasterStandbyEvent> {

	@Qualifier("global-counter")
	@Autowired
	private Counter counter;

	@Qualifier("local-counter")
	@Autowired
	private Counter localCounter;

	@Override
	public void onApplicationEvent(ContextMasterStandbyEvent event) {
		counter.reset();
		localCounter.reset();
	}

}
