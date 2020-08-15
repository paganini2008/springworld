package com.github.paganini2008.springworld.scheduler;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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
	
	public JobKey() {
	}

	JobKey(String groupName, String jobName, String jobClassName) {
		this.groupName = groupName;
		this.jobName = jobName;
		this.jobClassName = jobClassName;
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
	public String getIdentifier() {
		return getGroupName() + ":" + getJobName() + "@" + getJobClassName();
	}

	public String toString() {
		return getIdentifier();
	}

	@Override
	public int hashCode() {
		return getIdentifier().hashCode() * 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof JobKey) {
			return ((JobKey) obj).getIdentifier().equals(getIdentifier());
		}
		return false;
	}

	public static JobKey of(final Job job) {
		String groupName = job.getGroupName();
		String jobName = job.getJobName();
		String jobClassName = job.getJobClassName();
		return new JobKey(groupName, jobName, jobClassName);
	}

	@JsonCreator
	public static JobKey of(String identifier) {
		int startPosition = identifier.indexOf(":");
		int endPosition = identifier.lastIndexOf("@");
		String groupName = identifier.substring(0, startPosition);
		String jobName = identifier.substring(startPosition + 1, endPosition);
		String jobClassName = identifier.substring(endPosition + 1);
		return new JobKey(groupName, jobName, jobClassName);
	}

}
