package com.github.paganini2008.springworld.cluster.http;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.ClusterState;
import com.github.paganini2008.springworld.cluster.DefaultLeaderRecoveryCallback;
import com.github.paganini2008.springworld.cluster.utils.ApiRetryListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RetryableLeaderRecoveryCallback
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class RetryableLeaderRecoveryCallback extends DefaultLeaderRecoveryCallback implements ApiRetryListener {

	private static final String LEADER_PING_PATH = "/application/cluster/ping";

	@Autowired
	private LeaderHeartbeater leaderHeartbeater;

	@Override
	public void recover(ApplicationInfo leader) {
	}

	@Override
	public void onRetryBegin(String provider, Request request) {
		leaderContext.setClusterState(ClusterState.PROTECTED);
		leaderHeartbeater.cancel();
	}

	@Override
	public void onEachRetry(String provider, Request request, Throwable e) {
		ApplicationInfo leader = leaderContext.getLeader();
		log.warn("Attempt to keep heartbeating from cluster leader [{}]", leader);
	}

	@Override
	public void onRetryEnd(String provider, Request request, Throwable e) {
		ApplicationInfo leader = leaderContext.getLeader();
		log.warn("Cluster leader [{}] is exhausted", leader);
		if (leaderContext.getClusterState() == ClusterState.PROTECTED) {
			super.recover(leader);
		} else {
			log.warn("Now cluster state switch to {}", leaderContext.getClusterState());
		}
	}

	@Override
	public boolean matches(String provider, Request request) {
		return StringUtils.isNotBlank(request.getPath()) && request.getPath().equals(LEADER_PING_PATH);
	}

}
