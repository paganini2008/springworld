package com.github.paganini2008.springworld.cronkeeper;

import java.util.Date;

import com.github.paganini2008.springworld.cronkeeper.model.TriggerDescription;
import com.github.paganini2008.springworld.cronkeeper.model.TriggerDescription.Combined;
import com.github.paganini2008.springworld.cronkeeper.model.TriggerDescription.Cron;
import com.github.paganini2008.springworld.cronkeeper.model.TriggerDescription.Periodic;
import com.github.paganini2008.springworld.cronkeeper.model.TriggerDescription.Serial;

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

	public Cron getCron() {
		return triggerDescription.getCron();
	}

	public Periodic getPeriodic() {
		return triggerDescription.getPeriodic();
	}

	public Serial getSerial() {
		return triggerDescription.getSerial();
	}

	public Combined getCombined() {
		return triggerDescription.getCombined();
	}

	public TriggerBuilder setCronExpression(String cronExpression) {
		getCron().setExpression(cronExpression);
		return this;
	}

	public TriggerBuilder setPeriod(long period) {
		getPeriodic().setPeriod(period);
		return this;
	}

	public TriggerBuilder setFixedRate(boolean fixedRate) {
		getPeriodic().setFixedRate(fixedRate);
		return this;
	}

	public TriggerBuilder setSchedulingUnit(SchedulingUnit schedulingUnit) {
		getPeriodic().setSchedulingUnit(schedulingUnit);
		return this;
	}

	public TriggerBuilder setDependencies(JobKey[] dependencies) {
		getSerial().setDependencies(dependencies);
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
