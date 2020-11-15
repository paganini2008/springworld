package com.github.paganini2008.springworld.cluster.http;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterFollowerEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * LeaderHeartbeater
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class LeaderHeartbeater implements ApplicationListener<ApplicationClusterFollowerEvent>, Executable {

	public static final int DEFAULT_CHECKED_INTERVAL = 5;

	private Timer timer;

	@Autowired
	private LeaderService leaderService;

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		if (timer != null) {
			return;
		}
		timer = ThreadUtils.scheduleWithFixedDelay(this, DEFAULT_CHECKED_INTERVAL, TimeUnit.SECONDS);
	}

	@Override
	public boolean execute() {
		leaderService.ping();
		return true;
	}

	@Override
	public void onCancellation(Throwable e) {
		log.error(e.getMessage(), e);
		timer = null;
	}

}
