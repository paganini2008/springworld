package com.github.paganini2008.springworld.scheduler;

import com.github.paganini2008.devtools.date.DateUtils;

/**
 * 
 * PeriodicTrigger
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class PeriodicTrigger implements Trigger {

	private final long delay;
	private final SchedulingUnit delaySchedulingUnit;
	private final long period;
	private final SchedulingUnit periodSchedulingUnit;
	private final SchedulingMode schedulingMode;

	public PeriodicTrigger(SchedulingMode schedulingMode, long delay, SchedulingUnit delaySchedulingUnit, long period,
			SchedulingUnit periodSchedulingUnit) {
		this.delay = delay;
		this.delaySchedulingUnit = delaySchedulingUnit;
		this.period = period;
		this.periodSchedulingUnit = periodSchedulingUnit;
		this.schedulingMode = schedulingMode;
	}

	@Override
	public TriggerType getTriggerType() {
		return TriggerType.PERIODIC;
	}

	@Override
	public TriggerDescription getTriggerDescription() {
		TriggerDescription data = new TriggerDescription();
		data.setDelay(delay);
		data.setDelaySchedulingUnit(delaySchedulingUnit);
		data.setPeriod(period);
		data.setPeriodSchedulingUnit(periodSchedulingUnit);
		data.setSchedulingMode(schedulingMode);
		return data;
	}

	@Override
	public JobFuture fire(Scheduler scheduler, Job job, Object attachment) {
		long delayInMs = DateUtils.convertToMillis(delay, delaySchedulingUnit.getTimeUnit());
		long periodInMs = DateUtils.convertToMillis(period, periodSchedulingUnit.getTimeUnit());
		switch (schedulingMode) {
		case FIXED_DELAY:
			return scheduler.scheduleWithFixedDelay(job, attachment, delayInMs, periodInMs);
		case FIXED_RATE:
			return scheduler.scheduleAtFixedRate(job, attachment, delayInMs, periodInMs);
		}
		throw new IllegalStateException();
	}

}
