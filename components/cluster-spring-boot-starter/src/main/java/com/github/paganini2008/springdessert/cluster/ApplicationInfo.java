package com.github.paganini2008.springdessert.cluster;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * ApplicationInfo
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@JsonInclude(value = Include.NON_NULL)
@Getter
@Setter
public class ApplicationInfo implements Serializable, Comparable<ApplicationInfo> {

	private static final long serialVersionUID = 2499029995227541654L;

	private String id;
	private String clusterName;
	private String applicationName;
	private int weight;
	private long startTime;
	@JsonProperty("leader")
	private boolean isLeader;
	private ApplicationInfo leaderInfo;
	private String applicationContextPath;
	private Contact contact;

	public ApplicationInfo() {
	}

	ApplicationInfo(String id, String clusterName, String applicationName, ApplicationInfo leaderInfo) {
		Assert.hasNoText(id);
		Assert.hasNoText(clusterName);
		Assert.hasNoText(applicationName);

		this.id = id;
		this.clusterName = clusterName;
		this.applicationName = applicationName;
		this.leaderInfo = leaderInfo;
		this.isLeader = leaderInfo != null && id.equals(leaderInfo.getId());
	}

	@JsonIgnore
	public boolean isLeader() {
		return isLeader;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result |= clusterName.hashCode();
		return result | 37;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ApplicationInfo) {
			ApplicationInfo other = (ApplicationInfo) obj;
			return getClusterName().equals(other.getClusterName()) && getId().equals(other.getId());
		}
		return false;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new String[] { "leaderInfo" });
	}

	@Override
	public int compareTo(ApplicationInfo otherInfo) {
		String left = String.format("%s-%s-%s", id, clusterName, applicationName);
		String right = String.format("%s-%s-%s", otherInfo.getId(), otherInfo.getClusterName(), otherInfo.getApplicationName());
		return left.compareTo(right);
	}

}
