package com.github.paganini2008.springdessert.cluster.http;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.ClusterState;
import com.github.paganini2008.springdessert.cluster.LeaderContext;

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
public class LeaderHeartbeater implements ApplicationListener<ApplicationClusterFollowerEvent>, Executable,DisposableBean {

	public static final int DEFAULT_CHECKED_INTERVAL = 3;

	private Timer timer;

	@Autowired
	private LeaderService leaderService;

	@Autowired
	private LeaderContext leaderContext;

	private ApplicationInfo currentLeaderInfo;

	public void start() {
		if (timer == null) {
			timer = ThreadUtils.scheduleWithFixedDelay(this, DEFAULT_CHECKED_INTERVAL, TimeUnit.SECONDS);
			log.info("Keep heartbeating with cluster leader [{}]", currentLeaderInfo);
		}
	}

	public void cancel() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		this.currentLeaderInfo = event.getLeaderInfo();
		start();
	}

	@Override
	public boolean execute() {
		try {
			leaderService.ping();
		} catch (RestClientException e) {
			log.error(e.getMessage(), e);
		}
		return leaderContext.getClusterState() != ClusterState.FATAL;
	}

	@Override
	public void onCancellation(Throwable e) {
		if (e != null) {
			log.error(e.getMessage(), e);
		}
		timer = null;
	}

	@Override
	public void destroy() throws Exception {
		cancel();
	}
	
	

}