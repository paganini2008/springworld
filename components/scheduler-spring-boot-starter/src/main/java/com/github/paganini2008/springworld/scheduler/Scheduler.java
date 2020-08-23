package com.github.paganini2008.springworld.scheduler;

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

	JobFuture schedule(Job job, Object attachment, String cronExpression);

	JobFuture schedule(Job job, Object attachment, String cronExpression, Date startDate);

	JobFuture scheduleWithFixedDelay(Job job, Object attachment, long period, Date startDate);

	JobFuture scheduleAtFixedRate(Job job, Object attachment, long period, Date startDate);

	JobFuture scheduleWithDependency(Job job, String[] dependencies);

	JobFuture scheduleWithDependency(Job job, String[] dependencies, Date startDate);

	void runJob(Job job, Object attachment);

}
