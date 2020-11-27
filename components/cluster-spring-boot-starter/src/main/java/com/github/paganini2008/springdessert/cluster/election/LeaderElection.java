package com.github.paganini2008.springdessert.cluster.election;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * LeaderElection
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface LeaderElection {

	static final int DEFAULT_TIMEOUT = 60;

	void launch();

	void adapt(ApplicationEvent applicationEvent);

}
