package com.github.paganini2008.springworld.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.springworld.cluster.election.LeaderElection;

/**
 * 
 * DefaultLeaderRecoveryCallback
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class DefaultLeaderRecoveryCallback implements ApplicationListener<ApplicationClusterFollowerEvent>, LeaderRecoveryCallback {

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
	public void recover(ApplicationInfo leaderInfo) {
		if (instanceId.getClusterState() == ClusterState.PROTECTED) {
			log.info("Start to leader election recovery ...");
			electionObservable.notifyObservers(leaderInfo);
		}
	}

	protected Observable getObservable() {
		return electionObservable;
	}

}
