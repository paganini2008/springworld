package com.github.paganini2008.springdessert.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.paganini2008.springdessert.cluster.election.ConsistencyLeaderElection;
import com.github.paganini2008.springdessert.cluster.election.LeaderElection;

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

	@Value("${spring.application.cluster.consistency.leader-election.minimumParticipants:3}")
	private int minimumParticipants;

	@Autowired
	private LeaderElection leaderElection;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (leaderElection instanceof ConsistencyLeaderElection) {
			log.warn("Leader election will be launched if cluster's node equal or greater than {}.", minimumParticipants);
		} else {
			leaderElection.adapt(event);
		}
	}

}
