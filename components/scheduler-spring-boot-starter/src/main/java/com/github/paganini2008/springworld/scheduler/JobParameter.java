package com.github.paganini2008.springworld.scheduler;

import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobParameter
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobParameter {

	private String groupName;
	private String jobName;
	private String jobClassName;
	private String signature;
	private Object argument;

	public JobParameter() {
	}

	public JobParameter(String signature, String goupName, String jobName, String jobClassName, Object argument) {
		this.signature = signature;
		this.groupName = goupName;
		this.jobName = jobName;
		this.jobClassName = jobClassName;
		this.argument = argument;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
