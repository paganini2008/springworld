package com.github.paganini2008.springdessert.cluster.election;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterContext;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.HealthState;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DefaultLeaderRecovery
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class DefaultLeaderRecovery implements ApplicationListener<ApplicationClusterFollowerEvent>, LeaderRecovery {

	private final Observable electionObservable = Observable.unrepeatable();

	@Autowired
	private LeaderElection leaderElection;

	@Autowired
	protected ApplicationClusterContext leaderContext;

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		electionObservable.addObserver((ob, arg) -> {
			log.info("Launch new round leader election soon");
			leaderElection.launch();
		});
	}

	@Override
	public void recover(ApplicationInfo leaderInfo) {
		leaderContext.setHealthState(HealthState.UNLEADABLE);
		electionObservable.notifyObservers(leaderInfo);
	}

	protected Observable getObservable() {
		return electionObservable;
	}

}
