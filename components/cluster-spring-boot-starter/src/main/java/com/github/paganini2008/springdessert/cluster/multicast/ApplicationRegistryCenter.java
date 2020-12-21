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
 * @version 1.0
 */
@Slf4j
public class ApplicationRegistryCenter implements RegistryCenter, ApplicationListener<ApplicationMulticastEvent> {

	private final Map<String, List<ApplicationInfo>> applicationInfoHolder = new ConcurrentHashMap<String, List<ApplicationInfo>>();

	@Override
	public void registerApplication(ApplicationInfo applicationInfo) {
		String applicationName = applicationInfo.getApplicationName();
		List<ApplicationInfo> infoList = MapUtils.get(applicationInfoHolder, applicationName, () -> {
			return new CopyOnWriteArrayList<ApplicationInfo>();
		});
		infoList.add(applicationInfo);
		if (infoList.size() > 1) {
			Collections.sort(infoList);
		}
		log.info("Register application: [{}] to ApplicationRegistryCenter", applicationInfo);
	}

	@Override
	public void removeApplication(ApplicationInfo applicationInfo) {
		String applicationName = applicationInfo.getApplicationName();
		List<ApplicationInfo> infoList = applicationInfoHolder.get(applicationName);
		if (infoList != null) {
			infoList.remove(applicationInfo);
		}
		log.info("Remove application: [{}] from ApplicationRegistryCenter", applicationInfo);
	}

	@Override
	public List<ApplicationInfo> getApplications(String applicationName) {
		return applicationInfoHolder.get(applicationName);
	}

	@Override
	public int countOfApplication() {
		int total = 0;
		for (List<ApplicationInfo> list : applicationInfoHolder.values()) {
			total += list.size();
		}
		return total;
	}

	@Override
	public void onApplicationEvent(ApplicationMulticastEvent event) {
		ApplicationInfo applicationInfo = event.getApplicationInfo();
		if (event.getMulticastEventType() == MulticastEventType.ON_ACTIVE) {
			registerApplication(applicationInfo);
		} else if (event.getMulticastEventType() == MulticastEventType.ON_INACTIVE) {
			removeApplication(applicationInfo);
		}
	}

}
