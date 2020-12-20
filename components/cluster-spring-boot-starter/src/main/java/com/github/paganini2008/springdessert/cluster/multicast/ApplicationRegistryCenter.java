package com.github.paganini2008.springdessert.cluster.multicast;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastEvent.MulticastEventType;

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
public class ApplicationRegistryCenter implements ApplicationListener<ApplicationMulticastEvent> {

	private final Map<String, List<ApplicationInfo>> applicaitonInfoCache = new ConcurrentHashMap<String, List<ApplicationInfo>>();

	public List<ApplicationInfo> getApplications(String applicationName) {
		return applicaitonInfoCache.get(applicationName);
	}

	@Override
	public void onApplicationEvent(ApplicationMulticastEvent event) {
		if (event.getMulticastEventType() == MulticastEventType.ON_ACTIVE) {
			doIfActive(event.getApplicationInfo());
		} else if (event.getMulticastEventType() == MulticastEventType.ON_INACTIVE) {
			doIfInactive(event.getApplicationInfo());
		}
	}

	private void doIfActive(ApplicationInfo applicationInfo) {
		String applicationName = applicationInfo.getApplicationName();
		List<ApplicationInfo> infoList = MapUtils.get(applicaitonInfoCache, applicationName, () -> {
			return new CopyOnWriteArrayList<ApplicationInfo>();
		});
		infoList.add(applicationInfo);
		if (infoList.size() > 0) {
			Collections.sort(infoList);
		}
		log.info("Register application: [{}] to ApplicationRegistryCenter", applicationInfo);
	}

	private void doIfInactive(ApplicationInfo applicationInfo) {
		String applicationName = applicationInfo.getApplicationName();
		List<ApplicationInfo> infoList = applicaitonInfoCache.get(applicationName);
		if (infoList != null) {
			infoList.remove(applicationInfo);
		}
		log.info("Unregister application: [{}] to ApplicationRegistryCenter", applicationInfo);
	}

}
