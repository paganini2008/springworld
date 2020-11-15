package com.github.paganini2008.springworld.cluster.http;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.DefaultLeaderRecoveryCallback;

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

	private ApplicationInfo leaderInfo;

	@Override
	public void recover(ApplicationInfo leaderInfo) {
		this.leaderInfo = leaderInfo;
	}

	@Override
	public void onEachRetry(String provider, Request request, Throwable e) {
		log.warn("Attempt to keep heartbeating from cluster leader [{}]", leaderInfo);
	}

	@Override
	public void onRetryEnd(String provider, Request request, Throwable e) {
		log.warn("Cluster leader [{}] is exhausted", leaderInfo);
		super.recover(leaderInfo);
	}

	@Override
	public boolean matches(String provider, Request request) {
		return StringUtils.isNotBlank(request.getPath()) && request.getPath().equals(LEADER_PING_PATH);
	}

}
