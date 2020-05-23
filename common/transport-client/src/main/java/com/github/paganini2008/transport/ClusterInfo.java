package com.github.paganini2008.transport;

/**
 * 
 * ClusterInfo
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface ClusterInfo {

	static final String SPRING_CLUSTER_NAMESPACE = "spring:application:cluster:";
	static final String APPLICATION_KEY_PREFIX = "transport:application:";

	String getName();

	String[] getInstanceIds() throws Exception;

	int getInstanceCount() throws Exception;

	String[] getInstanceAddresses() throws Exception;

	default void releaseExternalResources() {
	}

}
