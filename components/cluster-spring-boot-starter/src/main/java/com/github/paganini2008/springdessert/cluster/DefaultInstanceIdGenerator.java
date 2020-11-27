package com.github.paganini2008.springdessert.cluster;

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

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Override
	public String generateInstanceId() {
		return clusterName + "@" + UUID.randomUUID().toString().replace("-", "");
	}

}
