package com.github.paganini2008.springdessert.cluster.http;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.multicast.MulticastGroupListener;
import com.github.paganini2008.springdessert.reditools.BeanNames;

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
public class ApplicationRegistryCenter implements MulticastGroupListener, RegistryCenter {

	private final Map<String, List<ApplicationInfo>> appInfoCache = new ConcurrentHashMap<String, List<ApplicationInfo>>();

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

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
