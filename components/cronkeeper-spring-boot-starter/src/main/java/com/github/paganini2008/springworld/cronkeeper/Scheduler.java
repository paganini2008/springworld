package com.github.paganini2008.springworld.cronkeeper;

import java.util.Date;

/**
 * 
 * Scheduler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface Scheduler {

	JobFuture schedule(Job job, Object attachment, Date startDate);

	JobFuture schedule(Job job, JobPeer[] jobPeers, Date startDate);

	JobFuture schedule(Job job, Object attachment, String cronExpression);

	JobFuture schedule(Job job, JobPeer[] jobPeers, String cronExpression);

	JobFuture schedule(Job job, Object attachment, String cronExpression, Date startDate);

	JobFuture scheduleWithFixedDelay(Job job, Object attachment, long period, Date startDate);

	JobFuture scheduleAtFixedRate(Job job, Object attachment, long period, Date startDate);

	JobFuture scheduleWithDependency(Job job, JobKey[] dependencies);

	JobFuture scheduleWithDependency(Job job, JobKey[] dependencies, Date startDate);

	void runJob(Job job, Object attachment);

}
