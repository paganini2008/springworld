package com.github.paganini2008.springworld.cronfall.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.paganini2008.springworld.cronfall.JacksonUtils;
import com.github.paganini2008.springworld.cronfall.TriggerType;

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
	private Date startDate;
	private Date endDate;
	private String triggerDescription;

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public void setTriggerType(int triggerType) {
		this.triggerType = TriggerType.valueOf(triggerType);
	}

	public void setTriggerDescription(String triggerDescription) {
		this.triggerDescription = triggerDescription;
	}

	public String getTriggerDescription() {
		return triggerDescription;
	}
	
	@JsonIgnore
	public TriggerDescription getTriggerDescriptionObject() {
		return JacksonUtils.parseJson(triggerDescription, TriggerDescription.class);
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

}
