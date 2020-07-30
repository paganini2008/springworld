package com.github.paganini2008.springworld.cluster;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * DefaultInstanceIdGenerator
 * 
 * @author Fred Feng
 * @version 1.0
 */
public class DefaultInstanceIdGenerator implements InstanceIdGenerator {

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Override
	public String generateInstanceId() {
		return clusterName + "@" + UUID.randomUUID().toString().replace("-", "");
	}

}
