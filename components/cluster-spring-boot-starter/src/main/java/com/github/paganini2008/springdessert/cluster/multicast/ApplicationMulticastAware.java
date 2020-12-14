package com.github.paganini2008.springdessert.cluster.multicast;

import static com.github.paganini2008.springdessert.cluster.ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.reditools.common.TtlKeeper;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

/**
 * 
 * ApplicationMulticastAware
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class ApplicationMulticastAware implements ApplicationListener<ContextRefreshedEvent> {

	public static final int DEFAULT_MULTICAST_GROUP_MEMBER_TTL = 5;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private TtlKeeper ttlKeeper;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
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
