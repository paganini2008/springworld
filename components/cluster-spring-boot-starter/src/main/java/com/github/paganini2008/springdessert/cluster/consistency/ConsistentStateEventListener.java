package com.github.paganini2008.springdessert.cluster.consistency;

import org.springframework.context.ApplicationListener;

/**
 * 
 * ConsistentStateEventListener
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class ConsistentStateEventListener implements ApplicationListener<ConsistencyRequestConfirmationEvent> {

	@Override
	public void onApplicationEvent(ConsistencyRequestConfirmationEvent event) {

	}

}
