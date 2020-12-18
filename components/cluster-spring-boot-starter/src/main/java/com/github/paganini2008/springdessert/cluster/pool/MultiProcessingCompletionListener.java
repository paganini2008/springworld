package com.github.paganini2008.springdessert.cluster.pool;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMessageListener;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

/**
 * 
 * MultiProcessingCompletionListener
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class MultiProcessingCompletionListener implements ApplicationMessageListener {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		redisMessageSender.sendMessage(((Return) message).getInvocation().getId(), message);
	}

	@Override
	public String getTopic() {
		return MultiProcessingCompletionListener.class.getName();
	}

}
