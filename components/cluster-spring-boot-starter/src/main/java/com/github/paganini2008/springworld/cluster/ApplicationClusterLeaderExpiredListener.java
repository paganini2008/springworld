package com.github.paganini2008.springworld.cluster;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;

import com.github.paganini2008.springworld.cluster.election.LeaderElection;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationClusterLeaderExpiredListener
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@SuppressWarnings("all")
public class ApplicationClusterLeaderExpiredListener implements ApplicationListener<RedisKeyExpiredEvent> {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private LeaderElection leaderElection;

	@Override
	public void onApplicationEvent(RedisKeyExpiredEvent event) {
		final String expiredKey = new String(event.getSource());
		final String heartbeatKey = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName;
		if (heartbeatKey.equals(expiredKey)) {
			log.info("The leader of application cluster '" + applicationName + "' is expired.");
			leaderElection.lookupLeader(event);
		}
	}

}
