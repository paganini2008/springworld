package com.github.paganini2008.springworld.jobswarm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springworld.cluster.ApplicationClusterNewLeaderEvent;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageHandler;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SerialJobDependencyListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class SerialJobDependencyListener implements ApplicationListener<ApplicationClusterNewLeaderEvent> {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void onApplicationEvent(ApplicationClusterNewLeaderEvent event) {
		RedisMessageHandler redisMessageHandler = ApplicationContextUtils.autowireBean(new SerialJobDependencyTrigger());
		redisMessageSender.subscribeChannel(SerialJobDependencyTrigger.BEAN_NAME, redisMessageHandler);
		log.info("Add JobDependencyProcessor to context successfully.");
	}

}
