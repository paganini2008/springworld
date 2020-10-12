package com.github.paganini2008.springworld.jobclick;

import java.util.Date;

import com.github.paganini2008.springworld.jobclick.model.TriggerDescription;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * CronTrigger
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Accessors(chain = true)
@Getter
@Setter
public class CronTrigger implements Trigger {

	private final TriggerDescription triggerDescription;
	private Date startDate;
	private Date endDate;
	private int repeatCount = -1;

	public CronTrigger(String cronExpression) {
		this.triggerDescription = new TriggerDescription(cronExpression);
	}

	protected CronTrigger(TriggerDescription triggerDescription) {
		this.triggerDescription = triggerDescription;
	}

	@Override
	public TriggerType getTriggerType() {
		return TriggerType.CRON;
	}

}
