package com.github.paganini2008.springdessert.jobsoup;

import com.github.paganini2008.springworld.cluster.ApplicationClusterLeaderEvent;

/**
 * 
 * SchedulerStarterListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface SchedulerStarterListener {

	void onApplicationEvent(ApplicationClusterLeaderEvent event);
}
