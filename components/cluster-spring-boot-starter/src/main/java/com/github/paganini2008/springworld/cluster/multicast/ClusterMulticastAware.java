package com.github.paganini2008.springworld.cluster.multicast;

import static com.github.paganini2008.springworld.cluster.ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springdessert.reditools.common.TtlKeeper;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;
import com.github.paganini2008.springworld.cluster.ApplicationClusterRefreshedEvent;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;

/**
 * 
 * ClusterMulticastAware
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ClusterMulticastAware implements ApplicationListener<ApplicationClusterRefreshedEvent> {

	private static final int DEFAULT_MULTICAST_GROUP_MEMBER_TTL = 5;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private TtlKeeper ttlKeeper;

	@Override
	public void onApplicationEvent(ApplicationClusterRefreshedEvent event) {
		keepAlive();
		introduceMyself();
	}

	private void keepAlive() {
		final String key = APPLICATION_CLUSTER_NAMESPACE + clusterName + ":member:" + instanceId.get();
		ApplicationInfo applicationInfo = instanceId.getApplicationInfo();
		ttlKeeper.keepAlive(key, applicationInfo, DEFAULT_MULTICAST_GROUP_MEMBER_TTL, 1, TimeUnit.SECONDS);
	}

	private void introduceMyself() {
		ApplicationInfo applicationInfo = instanceId.getApplicationInfo();
		final String channel = APPLICATION_CLUSTER_NAMESPACE + clusterName + ":active";
		redisMessageSender.sendMessage(channel, applicationInfo);
	}

}
