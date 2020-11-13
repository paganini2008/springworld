package com.github.paganini2008.springworld.cluster.election;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.springworld.cluster.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springworld.cluster.ClusterMode;
import com.github.paganini2008.springworld.cluster.InstanceId;

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
	private InstanceId instanceId;

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		electionObservable.addObserver((ob, arg) -> {
			leaderElection.launch();
		});
	}

	@Override
	public void recover() {
		if (instanceId.getClusterMode() == ClusterMode.PROTECTED) {
			log.info("Leader election recovery start ...");
			electionObservable.notifyObservers();
		}
	}

	protected Observable getObservable() {
		return electionObservable;
	}

}
