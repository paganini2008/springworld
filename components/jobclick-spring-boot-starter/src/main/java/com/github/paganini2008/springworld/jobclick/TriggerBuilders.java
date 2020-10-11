package com.github.paganini2008.springworld.jobclick;

import java.util.Date;

import com.github.paganini2008.springworld.jobclick.model.JobPeer;
import com.github.paganini2008.springworld.jobclick.model.TriggerDescription;
import com.github.paganini2008.springworld.jobclick.model.TriggerDescription.Milestone;

/**
 * 
 * TriggerBuilder
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public abstract class TriggerBuilders {

	public static abstract class TriggerBuilder {

		protected final TriggerDescription triggerDescription;
		protected Date startDate;
		protected Date endDate;
		protected int repeatCount = -1;

		protected TriggerBuilder(TriggerDescription triggerDescription) {
			this.triggerDescription = triggerDescription;
		}

		public TriggerBuilder setMilestone(JobPeer[] cooperators, Float goal) {
			triggerDescription.setMilestone(new Milestone(cooperators, goal));
			return this;
		}

		public TriggerBuilder setStartDate(Date startDate) {
			this.startDate = startDate;
			return this;
		}

		public TriggerBuilder setEndDate(Date endDate) {
			this.endDate = endDate;
			return this;
		}

		public TriggerBuilder setRepeatCount(int repeatCount) {
			this.repeatCount = repeatCount;
			return this;
		}

		public abstract Trigger build();

	}

	public static class CronTriggerBuilder extends TriggerBuilder {

		CronTriggerBuilder(String cronExpression) {
			super(new TriggerDescription(cronExpression));
		}

		public Trigger build() {
			CronTrigger trigger = new CronTrigger(triggerDescription);
			trigger.setStartDate(startDate).setEndDate(endDate).setRepeatCount(repeatCount);
			return trigger;
		}

	}

	public static class PeriodicTriggerBuilder extends TriggerBuilder {

		PeriodicTriggerBuilder(long period, SchedulingUnit schedulingUnit, boolean fixedRate) {
			super(new TriggerDescription(period, schedulingUnit, fixedRate));
		}

		public Trigger build() {
			PeriodicTrigger trigger = new PeriodicTrigger(triggerDescription);
			trigger.setStartDate(startDate).setEndDate(endDate).setRepeatCount(repeatCount);
			return trigger;
		}

	}

	public static class SerialTriggerBuilder extends TriggerBuilder {

		SerialTriggerBuilder(JobKey... jobKeys) {
			super(new TriggerDescription(jobKeys));
		}

		public Trigger build() {
			SerialTrigger trigger = new SerialTrigger(triggerDescription);
			trigger.setStartDate(startDate).setEndDate(endDate).setRepeatCount(repeatCount);
			return trigger;
		}

	}

	public static TriggerBuilder newCronTrigger(String cronExpression) {
		return new CronTriggerBuilder(cronExpression);
	}

	public static TriggerBuilder newPeriodicTrigger(long period, SchedulingUnit schedulingUnit, boolean fixedRate) {
		return new PeriodicTriggerBuilder(period, schedulingUnit, fixedRate);
	}

	public static TriggerBuilder newSerialTrigger(JobKey... jobKeys) {
		return new SerialTriggerBuilder(jobKeys);
	}

}
