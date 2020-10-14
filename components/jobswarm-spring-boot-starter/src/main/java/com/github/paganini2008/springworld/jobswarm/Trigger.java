package com.github.paganini2008.springworld.jobswarm;

import java.util.Date;

import com.github.paganini2008.springworld.jobswarm.model.TriggerDescription;

/**
 * 
 * Trigger
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface Trigger {

	Date getEndDate();

	Date getStartDate();
	
	int getRepeatCount();

	TriggerType getTriggerType();

	TriggerDescription getTriggerDescription();

}
