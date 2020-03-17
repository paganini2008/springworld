package com.github.paganini2008.transport;

/**
 * 
 * LookupAddress
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface LookupAddress {

	static final String CLUSTER_NAMESPACE = "spring:application:cluster:";

	static final String APPLICATION_KEY_PREFIX = "transport:application:";

	String[] getAddresses(String clusterName) throws Exception;

	void releaseExternalResources();

}
