package com.github.paganini2008.springworld.jobclick.model;

import com.github.paganini2008.devtools.beans.ToStringBuilder;
import com.github.paganini2008.springworld.jobclick.JobAction;
import com.github.paganini2008.springworld.jobclick.JobKey;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobActionParam
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobActionParam {

	private JobKey jobKey;
	private JobAction action;

	public JobActionParam() {
	}

	public JobActionParam(JobKey jobKey, JobAction action) {
		this.jobKey = jobKey;
		this.action = action;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}