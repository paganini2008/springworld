package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * SerializableTrigger
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class SerialTrigger implements Trigger {

	private final String[] dependencies;

	public SerialTrigger(String[] dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public TriggerType getTriggerType() {
		return TriggerType.SERIAL;
	}

	@Override
	public TriggerDescription getTriggerDescription() {
		TriggerDescription data = new TriggerDescription();
		data.setDependencies(dependencies);
		return data;
	}

	@Override
	public JobFuture fire(Scheduler scheduler, Job job, Object attachment) {
		return scheduler.scheduleWithDependency(job, dependencies);
	}

}
