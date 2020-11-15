package com.github.paganini2008.springworld.cluster.http;

import java.util.List;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;

public interface RegistryCenter {

	List<ApplicationInfo> getApplications(String applicationName);

	ApplicationInfo getLeader();

}