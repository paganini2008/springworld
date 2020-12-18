package com.github.paganini2008.springdessert.cluster.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationRegistryCenter
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class ApplicationRegistryCenter implements ApplicationMulticastListener, RegistryCenter {

	private final Map<String, List<ApplicationInfo>> appInfoCache = new ConcurrentHashMap<String, List<ApplicationInfo>>();

	public List<ApplicationInfo> getApplications(String applicationName) {
		return appInfoCache.get(applicationName);
	}

	@Override
	public void onActive(ApplicationInfo applicationInfo) {
		String applicationName = applicationInfo.getApplicationName();
		List<ApplicationInfo> infoList = MapUtils.get(appInfoCache, applicationName, () -> {
			return new CopyOnWriteArrayList<ApplicationInfo>();
		});
		infoList.add(applicationInfo);
		if (infoList.size() > 0) {
			Collections.sort(infoList);
		}
		log.info("Register application: [{}]", applicationInfo);
	}

	@Override
	public void onInactive(ApplicationInfo applicationInfo) {
		String applicationName = applicationInfo.getApplicationName();
		List<ApplicationInfo> infoList = appInfoCache.get(applicationName);
		if (infoList != null) {
			infoList.remove(applicationInfo);
		}
		log.info("Unregister application: [{}]", applicationInfo);
	}

}
