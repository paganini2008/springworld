package com.github.paganini2008.springworld.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.paganini2008.springworld.cluster.consistency.ConsistencyLeaderElectionListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationClusterAware enable the spring application to cluster mode when
 * spring context's initialization was done.
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class ApplicationClusterAware implements ApplicationListener<ContextRefreshedEvent> {

	public static final String APPLICATION_CLUSTER_NAMESPACE = "spring:application:cluster:";

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private LeaderElection leaderElection;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (leaderElection instanceof ConsistencyLeaderElectionListener) {
			log.warn("Leader election will be launched if cluster's node exceed 3.");
		} else {
			leaderElection.lookupLeader(event);
		}
	}

}
