package com.github.paganini2008.springdessert.cluster.multicast;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.springdessert.cluster.Constants;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastEvent.MulticastEventType;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageHandler;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

/**
 * 
 * ApplicationMulticastStarterListener
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationMulticastStarterListener implements RedisMessageHandler, ApplicationContextAware {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Autowired
	private InstanceId instanceId;

	private ApplicationContext applicationContext;

	@Override
	public void onMessage(String channel, Object message) {
		final ApplicationInfo applicationInfo = (ApplicationInfo) message;
		if (!applicationMulticastGroup.hasRegistered(applicationInfo)) {
			applicationMulticastGroup.registerCandidate(applicationInfo);
			redisMessageSender.sendMessage(getChannel(), instanceId.getApplicationInfo());
			applicationContext
					.publishEvent(new ApplicationMulticastEvent(applicationContext, applicationInfo, MulticastEventType.ON_ACTIVE));
		}
	}

	@Override
	public String getChannel() {
		return Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":active";
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
