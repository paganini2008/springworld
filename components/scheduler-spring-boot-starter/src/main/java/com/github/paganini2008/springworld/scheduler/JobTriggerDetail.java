package com.github.paganini2008.springworld.scheduler;

import java.io.Serializable;

import lombok.Getter;

/**
 * 
 * JobTriggerDetail
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
public class JobTriggerDetail implements Serializable {

	private static final long serialVersionUID = 866085363330905946L;
	private int jobId;
	private TriggerType triggerType;
	private TriggerDescription triggerDescription;

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public void setTriggerType(int triggerType) {
		this.triggerType = TriggerType.valueOf(triggerType);
	}

	public void setTriggerDescription(String triggerDescription) {
		this.triggerDescription = JacksonUtils.parseJson(triggerDescription, TriggerDescription.class);
	}

}
