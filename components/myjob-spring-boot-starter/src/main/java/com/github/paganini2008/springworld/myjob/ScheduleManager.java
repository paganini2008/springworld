package com.github.paganini2008.springworld.myjob;

/**
 * 
 * ScheduleManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ScheduleManager extends Lifecycle {

	void schedule(Job job);

	void doSchedule();

	boolean hasScheduled(JobKey jobKey);

	void unscheduleJob(JobKey jobKey);

	int countOfScheduling();

	JobFuture getJobFuture(JobKey jobKey);

}
