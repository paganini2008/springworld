package com.github.paganini2008.springdessert.cluster.http;

import java.util.List;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;

/**
 * 
 * RegistryCenter
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface RegistryCenter {

	List<ApplicationInfo> getApplications(String applicationName);

}