package com.github.paganini2008.springworld.cronkeeper;

import java.util.Date;

import com.github.paganini2008.springworld.cronkeeper.model.TriggerDescription;

/**
 * 
 * TriggerBuilder
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class TriggerBuilder {

	private final TriggerType triggerType;
	private Date startDate;
	private Date endDate;
	private TriggerDescription triggerDescription;

	TriggerBuilder(TriggerType triggerType) {
		this.triggerType = triggerType;
		this.triggerDescription = new TriggerDescription(triggerType);
	}

	public TriggerType getTriggerType() {
		return triggerType;
	}

	public Date getStartDate() {
		return startDate;
	}

	public TriggerBuilder setStartDate(Date startDate) {
		this.startDate = startDate;
		return this;
	}

	public Date getEndDate() {
		return endDate;
	}

	public TriggerBuilder setEndDate(Date endDate) {
		this.endDate = endDate;
		return this;
	}

	public TriggerBuilder setCronExpression(String cronExpression) {
		triggerDescription.getCron().setExpression(cronExpression);
		return this;
	}

	public TriggerBuilder setPeriod(long period) {
		triggerDescription.getPeriodic().setPeriod(period);
		return this;
	}

	public TriggerBuilder setFixedRate(boolean fixedRate) {
		triggerDescription.getPeriodic().setFixedRate(fixedRate);
		return this;
	}

	public TriggerBuilder setSchedulingUnit(SchedulingUnit schedulingUnit) {
		triggerDescription.getPeriodic().setSchedulingUnit(schedulingUnit);
		return this;
	}

	public TriggerBuilder setDependencies(JobKey[] dependencies) {
		triggerDescription.getSerial().setDependencies(dependencies);
		return this;
	}

	public TriggerDescription getTriggerDescription() {
		return triggerDescription;
	}

	public TriggerBuilder setTriggerDescription(TriggerDescription triggerDescription) {
		this.triggerDescription = triggerDescription;
		return this;
	}

	public static TriggerBuilder newTrigger(TriggerType triggerType) {
		return new TriggerBuilder(triggerType);
	}

}
