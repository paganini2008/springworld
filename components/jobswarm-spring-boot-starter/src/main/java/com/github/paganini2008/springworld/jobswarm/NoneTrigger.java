package com.github.paganini2008.springworld.jobswarm;

import java.util.Date;

import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription;

/**
 * 
 * NoneTrigger
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class NoneTrigger implements Trigger {

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
		return new TriggerDescription();
	}

}
