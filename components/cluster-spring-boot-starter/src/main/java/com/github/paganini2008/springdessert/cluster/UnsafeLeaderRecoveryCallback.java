package com.github.paganini2008.springdessert.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.springdessert.cluster.election.LeaderElection;

/**
 * 
 * UnsafeLeaderRecoveryCallback
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class UnsafeLeaderRecoveryCallback implements ApplicationListener<ApplicationClusterFollowerEvent>, LeaderRecoveryCallback {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final Observable electionObservable = Observable.unrepeatable();

	@Autowired
	private LeaderElection leaderElection;

	@Autowired
	protected LeaderContext leaderContext;

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		electionObservable.addObserver((ob, arg) -> {
			log.info("Launch new round leader election soon");
			leaderElection.launch();
		});
	}

	@Override
	public void recover(ApplicationInfo leaderInfo) {
		leaderContext.setClusterState(ClusterState.PROTECTED);
		electionObservable.notifyObservers(leaderInfo);
	}

	protected Observable getObservable() {
		return electionObservable;
	}

}