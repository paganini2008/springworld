package com.github.paganini2008.springworld.cronfall;

import java.io.Serializable;

import org.springframework.util.DigestUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.CharsetUtils;
import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
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
@Getter
public final class JobKey implements Serializable, Comparable<JobKey> {

	private static final long serialVersionUID = 3147872689801742981L;
	private String clusterName;
	private String groupName;
	private String jobName;
	private String jobClassName;

	public JobKey() {
	}

	JobKey(String clusterName, String groupName, String jobName, String jobClassName) {
		this.clusterName = clusterName;
		this.groupName = groupName;
		this.jobName = jobName;
		this.jobClassName = jobClassName;
	}

	@JsonIgnore
	public String getIndentifier() {
		final String repr = String.format("%s.%s.%s@%s", clusterName, groupName, jobName, jobClassName);
		return DigestUtils.md5DigestAsHex(repr.getBytes(CharsetUtils.UTF_8));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (clusterName != null ? 0 : clusterName.hashCode());
		result = prime * result + (groupName != null ? 0 : groupName.hashCode());
		result = prime * result + (jobName != null ? 0 : jobName.hashCode());
		result = prime * result + (jobClassName != null ? 0 : jobClassName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof JobKey) {
			JobKey jobKey = (JobKey) obj;
			return jobKey.getClusterName().equals(getClusterName()) && jobKey.getGroupName().equals(getGroupName())
					&& jobKey.getJobName().equals(getJobName()) && jobKey.getJobClassName().equals(getJobClassName());
		}
		return false;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int compareTo(JobKey otherKey) {
		String left = String.format("%s.%s.%s", clusterName, groupName, jobName);
		String right = String.format("%s.%s.%s", otherKey.getClusterName(), otherKey.getGroupName(), otherKey.getJobName());
		return left.compareTo(right);
	}

	public static JobKey of(final JobDefinition jobDef) {
		Assert.isNull(jobDef, "JobDef instance must be required.");
		String clusterName = jobDef.getClusterName();
		String groupName = jobDef.getGroupName();
		String jobName = jobDef.getJobName();
		String jobClassName = jobDef.getJobClassName();
		return new JobKey(clusterName, groupName, jobName, jobClassName);
	}

	public static JobKey by(String clusterName, String groupName, String jobName, String jobClassName) {
		return new JobKey(clusterName, groupName, jobName, jobClassName);
	}

}
