package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.context.ApplicationListener;

/**
 * 
 * ConsistentStateEventListener
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistentStateEventListener implements ApplicationListener<ConsistencyRequestConfirmationEvent> {

	@Override
	public void onApplicationEvent(ConsistencyRequestConfirmationEvent event) {

	}

}
