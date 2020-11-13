package com.github.paganini2008.springworld.cluster.election;

import com.github.paganini2008.springworld.cluster.http.ApiRetryListener;
import com.github.paganini2008.springworld.cluster.http.Request;

/**
 * 
 * RetryableLeaderRecoveryCallback
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RetryableLeaderRecoveryCallback extends UnsafeLeaderRecoveryCallback implements ApiRetryListener {

	private static final String LEADER_HEALTH_PATH = "/application/cluster/health";

	@Override
	public void onRetryBegin(String provider, Request request) {
		log.info("Start to check leader health");
	}

	@Override
	public void recover() {
	}

	@Override
	public void onRetryEnd(String provider, Request request, Throwable e) {
		super.recover();
	}

	@Override
	public boolean matches(String provider, Request request) {
		return request.getPath().equals(LEADER_HEALTH_PATH);
	}

}
