package com.github.paganini2008.springworld.cluster;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * LeaderElection
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface LeaderElection {

	void lookupLeader(ApplicationEvent applicationEvent);

}
