package com.github.paganini2008.springworld.jobclick;

import java.util.Date;

import com.github.paganini2008.springworld.jobclick.model.TriggerDescription;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * BasicTrigger
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Accessors(chain = true)
@Getter
@Setter
public class BasicTrigger implements Trigger {

	private final TriggerType triggerType;
	private TriggerDescription triggerDescription;
	private Date startDate;
	private Date endDate;
	private int repeatCount = -1;

	public BasicTrigger(TriggerType triggerType) {
		this.triggerType = triggerType;
		this.triggerDescription = triggerType.getTriggerDescription();
	}

}
