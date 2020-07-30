package com.github.paganini2008.springworld.cluster.election;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.InstanceId;

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

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Autowired
	private LeaderElection leaderElection;

	@Autowired
	private InstanceId instanceId;

	@Override
	public void onApplicationEvent(RedisKeyExpiredEvent event) {
		final String expiredKey = new String(event.getSource());
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		if (key.equals(expiredKey)) {
			log.info("The leader of application cluster '{}' is expired.", clusterName);
			instanceId.setLeaderInfo(null);
			leaderElection.lookupLeader(event);
		}
	}

}
