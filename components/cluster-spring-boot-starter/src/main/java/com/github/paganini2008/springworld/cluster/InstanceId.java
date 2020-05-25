package com.github.paganini2008.springworld.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.io.IOUtils;

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

	@Autowired
	private InstanceIdGenerator idGenerator;

	@Value("${spring.application.cluster.id:}")
	private String id;

	@Setter
	@Getter
	private String leaderId;

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
		return this.id.equals(leaderId);
	}

	public String toString() {
		return "ClusterId: " + get() + ", Master: " + isLeader();
	}

}
