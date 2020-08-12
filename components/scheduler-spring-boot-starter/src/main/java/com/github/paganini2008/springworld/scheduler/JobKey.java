package com.github.paganini2008.springworld.scheduler;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Setter;

/**
 * 
 * JobKey
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Setter
public final class JobKey implements Serializable {

	private static final long serialVersionUID = 3147872689801742981L;
	private String groupName;
	private String jobName;
	private String jobClassName;
	private String signature;

	public JobKey() {
	}

	public JobKey(String groupName, String jobName, String jobClassName, String signature) {
		this.groupName = groupName;
		this.jobName = jobName;
		this.jobClassName = jobClassName;
		this.signature = signature;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getJobName() {
		return jobName;
	}

	public String getJobClassName() {
		return jobClassName;
	}

	@JsonValue
	public String getSignature() {
		return signature;
	}

	@JsonCreator
	public static JobKey of(String signature) {
		int startPosition = signature.indexOf(":");
		int endPosition = signature.lastIndexOf("@");
		String groupName = signature.substring(0, startPosition);
		String jobName = signature.substring(startPosition + 1, endPosition);
		String jobClassName = signature.substring(endPosition + 1);
		return new JobKey(groupName, jobName, jobClassName, signature);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int hashCode() {
		return getSignature().hashCode() * 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof JobKey) {
			return ((JobKey) obj).getSignature().equals(getSignature());
		}
		return false;
	}

}
