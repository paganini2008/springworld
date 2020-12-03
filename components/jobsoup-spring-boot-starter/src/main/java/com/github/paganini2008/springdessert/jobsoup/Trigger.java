package com.github.paganini2008.springdessert.jobsoup;

import java.util.Date;

import com.github.paganini2008.springdessert.jobsoup.model.TriggerDescription;

/**
 * 
 * Trigger
 * 
 * @author Jimmy Hoff
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
