package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.redisplus.BeanNames;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageHandler;

/**
 * 
 * ApplicationInactiveListener
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationInactiveListener implements RedisMessageHandler {

	@Autowired
	private ClusterMulticastGroup multicastGroup;

	@Autowired
	private ClusterMulticastListenerContainer multicastListenerContainer;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Value("${spring.application.name}")
	private String applicationName;

	@Override
	public void onMessage(String channel, Object message) {
		final ApplicationInfo applicationInfo = (ApplicationInfo) message;
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName;
		redisTemplate.opsForList().remove(key, 1, applicationInfo);
		
		multicastGroup.removeChannel(applicationInfo.getId());
		multicastListenerContainer.fireOnInactive(applicationInfo);
	}

	@Override
	public String getChannel() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":member:*";
	}

}
