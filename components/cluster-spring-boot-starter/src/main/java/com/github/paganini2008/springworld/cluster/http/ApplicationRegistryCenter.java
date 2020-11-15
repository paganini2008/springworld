package com.github.paganini2008.springworld.cluster.http;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.multicast.ClusterStateChangeListener;
import com.github.paganini2008.springworld.reditools.BeanNames;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationRegistryCenter
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ApplicationRegistryCenter implements ClusterStateChangeListener, RegistryCenter {

	private final Map<String, List<ApplicationInfo>> appInfoCache = new ConcurrentHashMap<String, List<ApplicationInfo>>();

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private InstanceId instanceId;

	public List<ApplicationInfo> getApplications(String applicationName) {
		return appInfoCache.get(applicationName);
	}

	public ApplicationInfo getLeader() {
		return instanceId.getLeaderInfo();
	}

	@Override
	public void onActive(ApplicationInfo applicationInfo) {
		String applicationName = applicationInfo.getApplicationName();
		List<ApplicationInfo> infoList = MapUtils.get(appInfoCache, applicationName, () -> {
			return new CopyOnWriteArrayList<ApplicationInfo>();
		});
		infoList.add(applicationInfo);
		// printSelf();
		System.out.println("appName: " + applicationName + " 加入");
	}

	@Override
	public void onInactive(ApplicationInfo applicationInfo) {
		String applicationName = applicationInfo.getApplicationName();
		List<ApplicationInfo> infoList = appInfoCache.get(applicationName);
		if (infoList != null) {
			infoList.remove(applicationInfo);
		}
		System.out.println("appName: " + applicationName + " 离开");
	}

	public void printSelf() {
		for (Map.Entry<String, List<ApplicationInfo>> entry : appInfoCache.entrySet()) {
			log.info("Application: " + entry.getKey());
			log.info("Members: " + entry.getValue());
		}
	}

}
