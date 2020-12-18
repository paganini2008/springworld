package com.github.paganini2008.springdessert.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springdessert.cluster.election.LeaderElection;
import com.github.paganini2008.springdessert.cluster.election.LeaderElectionListener;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastEvent.MulticastEventType;

/**
 * 
 * ApplicationLeaderElectionListener
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationLeaderElectionListener implements LeaderElectionListener, ApplicationListener<ApplicationMulticastEvent> {

	@Autowired
	private LeaderElection leaderElection;

	@Override
	public void onApplicationEvent(ApplicationMulticastEvent applicationEvent) {
		if (applicationEvent.getMulticastEventType() == MulticastEventType.ON_ACTIVE) {
			leaderElection.onTriggered(applicationEvent);
		}
	}

}
