package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * Scheduler
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface Scheduler {

	Future schedule(Job job, Object attachment, String cron);

	Future scheduleWithFixedDelay(Job job, Object attachment, long delay, long period);

	Future scheduleAtFixedRate(Job job, Object attachment, long delay, long period);

	void runJob(Job job, Object attachment);

}
