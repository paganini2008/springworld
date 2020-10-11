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
 * SerialTrigger
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Accessors(chain = true)
@Getter
@Setter
public class SerialTrigger implements Trigger {

	private Date startDate;
	private Date endDate;
	private int repeatCount;
	private final TriggerDescription triggerDescription;

	public SerialTrigger(JobKey... jobKeys) {
		triggerDescription = new TriggerDescription(jobKeys);
	}

	protected SerialTrigger(TriggerDescription triggerDescription) {
		this.triggerDescription = triggerDescription;
	}

	public SerialTrigger setMilestone(JobPeer[] cooperators, Float goal) {
		triggerDescription.setMilestone(new Milestone(cooperators, goal));
		return this;
	}

	@Override
	public TriggerType getTriggerType() {
		return TriggerType.SERIAL;
	}

}
