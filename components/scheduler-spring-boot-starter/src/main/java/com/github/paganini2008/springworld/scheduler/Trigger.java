package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * Trigger
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface Trigger {
	
	TriggerType getTriggerType();

	TriggerDescription getTriggerDescription();

	JobFuture fire(Scheduler scheduler, Job job, Object attachment);

}
