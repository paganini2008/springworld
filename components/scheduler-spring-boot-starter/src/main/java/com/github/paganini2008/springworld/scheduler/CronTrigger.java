package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * CronTrigger
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class CronTrigger implements Trigger {

	private final String cronExpression;

	public CronTrigger(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Override
	public TriggerType getTriggerType() {
		return TriggerType.CRON;
	}

	@Override
	public TriggerDescription getTriggerDescription() {
		TriggerDescription data = new TriggerDescription();
		data.setCron(cronExpression);
		return data;
	}

	@Override
	public JobFuture fire(Scheduler scheduler, Job job, Object attachment) {
		return scheduler.schedule(job, attachment, cronExpression);
	}

}
