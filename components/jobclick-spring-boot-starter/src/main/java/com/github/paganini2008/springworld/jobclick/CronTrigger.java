package com.github.paganini2008.springworld.jobclick;

import java.util.Date;

import com.github.paganini2008.springworld.jobclick.model.JobPeer;
import com.github.paganini2008.springworld.jobclick.model.TriggerDescription;
import com.github.paganini2008.springworld.jobclick.model.TriggerDescription.Milestone;

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

	public CronTrigger setMilestone(JobPeer[] cooperators, Float goal) {
		triggerDescription.setMilestone(new Milestone(cooperators, goal));
		return this;
	}

	@Override
	public TriggerType getTriggerType() {
		return TriggerType.CRON;
	}

}
