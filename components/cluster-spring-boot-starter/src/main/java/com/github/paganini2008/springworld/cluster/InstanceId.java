package com.github.paganini2008.springworld.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.io.IOUtils;
import com.github.paganini2008.devtools.net.NetUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * InstanceId
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public final class InstanceId {

	private final long startTime = System.currentTimeMillis();

	@Autowired
	private InstanceIdGenerator idGenerator;

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.application.cluster.id:}")
	private String id;

	@Value("${spring.application.cluster.hostName:}")
	private String hostName;

	@Value("${server.port}")
	private int serverPort;

	@Setter
	@Getter
	private ApplicationInfo leaderInfo;

	@Getter
	@Value("${spring.application.cluster.weight:1}")
	private int weight;

	public String get() {
		if (StringUtils.isBlank(id)) {
			synchronized (this) {
				if (StringUtils.isBlank(id)) {
					id = idGenerator.generateInstanceId();
					log.info(IOUtils.NEWLINE + "\tGenerate the instanceId: " + id);
				}
			}
		}
		return id;
	}

	public boolean isLeader() {
		if (leaderInfo == null) {
			return false;
		}
		return get().equals(leaderInfo.getId());
	}

	public String getHostName() {
		if (StringUtils.isBlank(hostName)) {
			hostName = NetUtils.getLocalHost();
		}
		return hostName;
	}

	public long getStartTime() {
		return startTime;
	}

	public ApplicationInfo getApplicationInfo() {
		return new ApplicationInfo(get(), clusterName, applicationName, getHostName(), serverPort, getWeight(), getStartTime(),
				getLeaderInfo());
	}

	public String toString() {
		return "InstanceId: " + get() + ", Leader: " + isLeader();
	}

}
