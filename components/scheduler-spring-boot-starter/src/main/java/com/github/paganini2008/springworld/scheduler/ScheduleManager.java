package com.github.paganini2008.springworld.scheduler;

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

	boolean hasScheduled(Job job);

	void unscheduleJob(Job job);

	int countOfScheduling();

	JobFuture getFuture(Job job);

}
