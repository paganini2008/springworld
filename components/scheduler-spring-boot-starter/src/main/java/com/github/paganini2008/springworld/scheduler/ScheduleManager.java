package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * ScheduleManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface ScheduleManager {

	void schedule(Job job, Object attachment);

	void doSchedule();

	void runJob(Job job, Object attachment);

	boolean hasScheduled(Job job);

	void unscheduleJob(Job job);

	int countOfScheduling();
	
	Future getFuture(Job job);
	
	void close();

}
