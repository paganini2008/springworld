package com.github.paganini2008.springworld.cluster.pool;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

/**
 * 
 * MultiProcessingCompletionListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class MultiProcessingCompletionListener implements ClusterMessageListener {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		redisMessageSender.sendMessage(((Signature) message).getId(), message);
	}

	@Override
	public String getTopic() {
		return MultiProcessingCompletionListener.class.getName();
	}
	
	

}
