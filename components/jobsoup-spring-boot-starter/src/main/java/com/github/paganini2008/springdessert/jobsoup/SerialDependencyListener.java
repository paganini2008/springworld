package com.github.paganini2008.springdessert.jobsoup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springdessert.cluster.ApplicationClusterLeaderEvent;
import com.github.paganini2008.springdessert.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageHandler;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SerialDependencyListener
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class SerialDependencyListener implements ApplicationListener<ApplicationClusterLeaderEvent> {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void onApplicationEvent(ApplicationClusterLeaderEvent event) {
		RedisMessageHandler redisMessageHandler = ApplicationContextUtils.instantiateClass(SerialDependencyTrigger.class);
		redisMessageSender.subscribeChannel(SerialDependencyTrigger.BEAN_NAME, redisMessageHandler);
		log.info("SerialDependencyTrigger initialize successfully.");
	}

}
