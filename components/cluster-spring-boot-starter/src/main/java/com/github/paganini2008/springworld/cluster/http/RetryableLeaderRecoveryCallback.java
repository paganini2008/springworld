package com.github.paganini2008.springworld.cluster.http;

import com.github.paganini2008.springworld.cluster.DefaultLeaderRecoveryCallback;

/**
 * 
 * RetryableLeaderRecoveryCallback
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RetryableLeaderRecoveryCallback extends DefaultLeaderRecoveryCallback implements ApiRetryListener {

	private static final String LEADER_PING_PATH = "/application/cluster/ping";

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
		return request.getPath().equals(LEADER_PING_PATH);
	}

}
