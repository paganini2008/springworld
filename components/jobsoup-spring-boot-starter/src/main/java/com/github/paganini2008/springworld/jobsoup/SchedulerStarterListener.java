package com.github.paganini2008.springworld.jobsoup;

import com.github.paganini2008.springworld.cluster.ApplicationClusterNewLeaderEvent;

/**
 * 
 * SchedulerStarterListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface SchedulerStarterListener {

	void onApplicationEvent(ApplicationClusterNewLeaderEvent event);
}
