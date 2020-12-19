package com.github.paganini2008.springdessert.jobsoup;

import com.github.paganini2008.springdessert.cluster.election.ApplicationClusterLeaderEvent;

/**
 * 
 * SchedulerStarterListener
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface SchedulerStarterListener {

	void onApplicationEvent(ApplicationClusterLeaderEvent event);
}
