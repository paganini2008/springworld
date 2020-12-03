package com.github.paganini2008.springdessert.jobsoup;

import java.util.Date;

import com.github.paganini2008.springdessert.jobsoup.model.TriggerDescription;

/**
 * 
 * NoneTrigger
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class NoneTrigger implements Trigger {

	private final TriggerDescription triggerDescription;

	public NoneTrigger() {
		this.triggerDescription = new TriggerDescription();
	}

	@Override
	public Date getEndDate() {
		return null;
	}

	@Override
	public Date getStartDate() {
		return null;
	}

	@Override
	public int getRepeatCount() {
		return -1;
	}

	@Override
	public TriggerType getTriggerType() {
		return TriggerType.NONE;
	}

	@Override
	public TriggerDescription getTriggerDescription() {
		return triggerDescription;
	}

}
