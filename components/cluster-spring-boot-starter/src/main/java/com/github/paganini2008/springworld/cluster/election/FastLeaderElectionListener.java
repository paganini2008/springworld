package com.github.paganini2008.springworld.cluster.election;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.LeaderContext;
import com.github.paganini2008.springworld.cluster.LeaderRecoveryCallback;

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

	@Value("${spring.application.cluster.name}")
	private String clusterName;
	
	@Autowired
	private InstanceId instanceId;

	@Autowired
	private LeaderContext leaderContext;

	@Autowired
	private LeaderRecoveryCallback recoveryCallback;

	@Override
	public void onApplicationEvent(final RedisKeyExpiredEvent event) {
		final String expiredKey = new String(event.getSource());
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		if (key.equals(expiredKey)) {
			log.info("The leader of application cluster '{}' is expired.", clusterName);
			ApplicationInfo leaderInfo = instanceId.getLeaderInfo();
			
			
			instanceId.setLeaderInfo(null);

			recoveryCallback.recover(leaderInfo);
		}
	}

}
