package com.github.paganini2008.springdessert.cluster.http;

import java.util.List;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;

/**
 * 
 * RegistryCenter
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface RegistryCenter {

	List<ApplicationInfo> getApplications(String applicationName);

	ApplicationInfo getLeader();

}