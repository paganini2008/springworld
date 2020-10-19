package com.github.paganini2008.springworld.jobswarm.utils;

import java.util.Date;

import com.github.paganini2008.springworld.jobswarm.BasicTrigger;
import com.github.paganini2008.springworld.jobswarm.SchedulingUnit;
import com.github.paganini2008.springworld.jobswarm.Trigger;
import com.github.paganini2008.springworld.jobswarm.TriggerType;
import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * TriggerBuilder
 * @author Fred Feng
 *
 * @since 1.0
 */
@Accessors(chain = true)
@Setter
@Getter
public final class TriggerBuilder {
	
	private final TriggerDescription triggerDescription;
	private final TriggerType triggerType;
	private Date startDate;
	private Date endDate;
	private int repeatCount = -1;

	TriggerBuilder() {
		this.triggerDescription = new TriggerDescription();
		this.triggerType = TriggerType.NONE;
	}

	TriggerBuilder(String cronExpression) {
		this.triggerDescription = new TriggerDescription(cronExpression);
		this.triggerType = TriggerType.CRON;
	}

	TriggerBuilder(long period, SchedulingUnit schedulingUnit, boolean fixedRate) {
		this.triggerDescription = new TriggerDescription(period, schedulingUnit, fixedRate);
		this.triggerType = TriggerType.PERIODIC;
	}

	public Trigger build() {
		return new BasicTrigger(triggerType).setTriggerDescription(triggerDescription).setStartDate(startDate).setEndDate(endDate)
				.setRepeatCount(repeatCount);
	}

	public static TriggerBuilder newTrigger() {
		return new TriggerBuilder();
	}

	public static TriggerBuilder newTrigger(String cronExpression) {
		return new TriggerBuilder(cronExpression);
	}

	public static TriggerBuilder newTrigger(long period, SchedulingUnit schedulingUnit, boolean fixedRate) {
		return new TriggerBuilder(period, schedulingUnit, fixedRate);
	}
}
