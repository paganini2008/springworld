package com.github.paganini2008.springworld.jobclick;

import java.util.Date;

import com.github.paganini2008.springworld.jobclick.model.JobPeer;

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

	JobFuture schedule(JobTeam jobTeam, Date startDate);

	JobFuture schedule(Job job, Object attachment, String cronExpression);

	JobFuture schedule(JobTeam jobTeam, String cronExpression);

	JobFuture schedule(Job job, Object attachment, String cronExpression, Date startDate);

	JobFuture schedule(JobTeam jobTeam, String cronExpression, Date startDate);

	JobFuture scheduleWithFixedDelay(Job job, Object attachment, long period, Date startDate);

	JobFuture scheduleAtFixedRate(Job job, Object attachment, long period, Date startDate);

	JobFuture scheduleWithFixedDelay(JobTeam jobTeam, long period, Date startDate);

	JobFuture scheduleAtFixedRate(JobTeam jobTeam, long period, Date startDate);

	JobFuture scheduleWithDependency(Job job, JobKey[] dependencies);

	JobFuture scheduleWithDependency(Job job, JobKey[] dependencies, Date startDate);

	JobFuture scheduleWithDependency(JobTeam jobTeam, JobKey[] dependencies);

	JobFuture scheduleWithDependency(JobTeam jobTeam, JobKey[] dependencies, Date startDate);

	void runJob(Job job, Object attachment);

	JobTeam createJobTeam(Job job, JobPeer[] jobPeers);

}
