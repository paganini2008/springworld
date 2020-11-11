package com.github.paganini2008.springworld.cluster;

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
public class ApplicationRegistryCenter implements ApplicationListener<ApplicationClusterRefreshedEvent> {

	private final Map<String, List<ApplicationInfo>> appInfoCache = new ConcurrentHashMap<String, List<ApplicationInfo>>();

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private ApplicationInfo leaderInfo;

	public List<ApplicationInfo> getApplicationInfos(String applicationName) {
		return appInfoCache.get(applicationName);
	}

	public ApplicationInfo getLeaderInfo() {
		return leaderInfo;
	}

	@Override
	public void onApplicationEvent(ApplicationClusterRefreshedEvent event) {
		this.leaderInfo = event.getLeaderInfo();
		this.appInfoCache.clear();
		final String namespace = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		List<Object> dataList = redisTemplate.opsForList().range(namespace, 0, -1);
		ApplicationInfo appInfo;
		for (Object data : dataList) {
			appInfo = (ApplicationInfo) data;
			MapUtils.get(appInfoCache, appInfo.getApplicationName(), () -> {
				return new CopyOnWriteArrayList<ApplicationInfo>();
			}).add(appInfo);
		}
		printSelf();
	}

	public void printSelf() {
		for (Map.Entry<String, List<ApplicationInfo>> entry : appInfoCache.entrySet()) {
			log.info("Application: " + entry.getKey());
			log.info("Members: " + entry.getValue());
		}
	}

}
