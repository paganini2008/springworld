package com.github.paganini2008.springworld.cluster.election;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * FastLeaderElectionListener
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@SuppressWarnings("all")
public class FastLeaderElectionListener implements ApplicationListener<RedisKeyExpiredEvent>, LeaderElectionListener {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private LeaderElection leaderElection;

	@Override
	public void onApplicationEvent(RedisKeyExpiredEvent event) {
		final String expiredKey = new String(event.getSource());
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName;
		if (key.equals(expiredKey)) {
			log.info("The leader of application cluster '{}' is expired.", applicationName);
			leaderElection.lookupLeader(event);

			processWhenLeaderExpired(key);
		}
	}

	protected void processWhenLeaderExpired(String key) {
	}

}
