package com.github.paganini2008.springworld.cluster.scheduler;

/**
 * 
 * JobManager
 * 
 * @author Fred Feng
 * @version 1.0
 */
public interface JobManager {

	default void configure() {
	}
	
	void addJob(Job job);

	void deleteJob(Job job);
	
	boolean hasJob(Job job);

	void schedule(Job job);

	boolean hasScheduled(Job job);

	void pauseJob(Job job);

	void resumeJob(Job job);

	void unscheduleJob(Job job);
	
	void testJob(Job job);

	void runNow();

	int countOfScheduling();

	void close();

}
