package com.github.paganini2008.springworld.cronfall;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springworld.cluster.ApplicationClusterNewLeaderEvent;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageHandler;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobDependencyDetector
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JobDependencyDetector implements ApplicationListener<ApplicationClusterNewLeaderEvent> {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void onApplicationEvent(ApplicationClusterNewLeaderEvent event) {
		RedisMessageHandler redisMessageHandler = ApplicationContextUtils.autowireBean(new JobDependencyProcessor());
		redisMessageSender.subscribeChannel(JobDependencyProcessor.BEAN_NAME, redisMessageHandler);
		log.info("Add JobDependencyProcessor to context successfully.");
	}

}
