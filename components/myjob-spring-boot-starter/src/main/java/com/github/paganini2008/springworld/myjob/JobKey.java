package com.github.paganini2008.springworld.myjob;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.StringUtils;

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
	private static final String IDENTIFIER_FORMAT = "%s.%s@%s";
	private static final String IDENTIFIER_PATTERN = "[\\w\\-]+\\.\\w+\\@[\\w.]+";
	private String groupName;
	private String jobName;
	private String jobClassName;
	private String identifier;

	public JobKey() {
	}

	JobKey(String groupName, String jobName, String jobClassName) {
		this.groupName = groupName;
		this.jobName = jobName;
		this.jobClassName = jobClassName;
		this.identifier = String.format(IDENTIFIER_FORMAT, groupName, jobName, jobClassName);
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
		return identifier;
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

	public static JobKey of(final JobDef jobDef) {
		Assert.isNull(jobDef, "JobDef instance must be required.");
		String groupName = jobDef.getGroupName();
		String jobName = jobDef.getJobName();
		String jobClassName = jobDef.getJobClassName();
		return new JobKey(groupName, jobName, jobClassName);
	}

	@JsonCreator
	public static JobKey of(String identifier) {
		if (StringUtils.isBlank(identifier) || !identifier.matches(IDENTIFIER_PATTERN)) {
			throw new IllegalArgumentException("Invalid identifier: " + identifier);
		}
		int startPosition = identifier.indexOf(".");
		int endPosition = identifier.lastIndexOf("@");
		String groupName = identifier.substring(0, startPosition);
		String jobName = identifier.substring(startPosition + 1, endPosition);
		String jobClassName = identifier.substring(endPosition + 1);
		return new JobKey(groupName, jobName, jobClassName);
	}

}
