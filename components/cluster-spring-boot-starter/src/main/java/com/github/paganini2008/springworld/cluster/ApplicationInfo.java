package com.github.paganini2008.springworld.cluster;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Getter
@Setter
public class ApplicationInfo implements Serializable {

	private static final long serialVersionUID = 2499029995227541654L;

	private String id;
	private String applicationName;
	private String hostName;
	private int port;
	private int weight;
	private long startTime;
	private Map<String, String> description;
	private ApplicationInfo leaderInfo;

	public ApplicationInfo() {
	}

	public ApplicationInfo(String id, String applicationName, String hostName, int port, int weight, long startTime,
			ApplicationInfo leaderInfo) {
		Assert.hasNoText("id");
		Assert.hasNoText("applicationName");
		this.id = id;
		this.applicationName = applicationName;
		this.hostName = hostName;
		this.port = port;
		this.weight = weight;
		this.startTime = startTime;
		this.leaderInfo = leaderInfo;
	}

	@JsonIgnore
	public boolean isLeader() {
		if (leaderInfo == null) {
			return false;
		}
		return id.equals(leaderInfo.getId());
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result |= applicationName.hashCode();
		return result | 37;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ApplicationInfo) {
			if (obj == this) {
				return true;
			}
			ApplicationInfo other = (ApplicationInfo) obj;
			return getApplicationName().equals(other.getApplicationName()) && getId().equals(other.getId());
		}
		return false;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new String[] { "leaderInfo", "leader" });
	}

}
