package com.github.paganini2008.springdessert.cluster.multicast;

import java.util.List;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;

/**
 * 
 * RegistryCenter
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface RegistryCenter {

	void registerApplication(ApplicationInfo applicationInfo);

	void removeApplication(ApplicationInfo applicationInfo);

	List<ApplicationInfo> getApplications(String applicationName);

	int countOfApplication();

}
