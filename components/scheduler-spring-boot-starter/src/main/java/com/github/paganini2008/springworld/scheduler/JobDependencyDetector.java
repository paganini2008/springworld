package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.StringUtils;
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

	private String clusterName;

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	@Override
	public void onApplicationEvent(ApplicationClusterNewLeaderEvent event) {
		if (StringUtils.isBlank(clusterName)) {
			clusterName = ApplicationContextUtils.getRequiredProperty("spring.application.cluster.name");
		}
		RedisMessageHandler redisMessageHandler = ApplicationContextUtils.autowireBean(new JobDependencyProcessor(clusterName));
		redisMessageSender.subscribeChannel(JobDependencyProcessor.BEAN_NAME, redisMessageHandler);
		log.info("Add JobDependencyProcessor to context successfully.");
	}

}
