package com.github.paganini2008.springworld.cluster.http;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.reditools.BeanNames;
import com.github.paganini2008.springworld.cluster.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.MulticastGroupListener;

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
public class ApplicationRegistryCenter
		implements ApplicationListener<ApplicationClusterFollowerEvent>, MulticastGroupListener, RegistryCenter {

	private final Map<String, List<ApplicationInfo>> appInfoCache = new ConcurrentHashMap<String, List<ApplicationInfo>>();

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private ApplicationInfo leaderInfo;

	public List<ApplicationInfo> getApplications(String applicationName) {
		return appInfoCache.get(applicationName);
	}

	public ApplicationInfo getLeader() {
		return leaderInfo;
	}

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		this.leaderInfo = event.getLeaderInfo();
	}

	@Override
	public void onActive(ApplicationInfo applicationInfo) {
		String applicationName = applicationInfo.getApplicationName();
		List<ApplicationInfo> infoList = MapUtils.get(appInfoCache, applicationName, () -> {
			return new CopyOnWriteArrayList<ApplicationInfo>();
		});
		infoList.add(applicationInfo);
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
