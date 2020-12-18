package com.github.paganini2008.springdessert.cluster;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.springdessert.cluster.election.LeaderElection;
import com.github.paganini2008.springdessert.cluster.election.LeaderElectionListener;
import com.github.paganini2008.springdessert.cluster.http.EnableRestClient;
import com.github.paganini2008.springdessert.cluster.http.LeaderService;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationLeaderElection;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationLeaderElectionListener;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationLeaderRecoveryListener;

/**
 * 
 * ApplicationClusterLeaderConfig
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@EnableRestClient(include = { LeaderService.class })
@Configuration
public class ApplicationClusterLeaderConfig {

	@Bean
	public LeaderElectionListener leaderElectionListener() {
		return new ApplicationLeaderElectionListener();
	}

	@Bean
	public ApplicationLeaderRecoveryListener applicationLeaderRecoveryListener() {
		return new ApplicationLeaderRecoveryListener();
	}

	@ConditionalOnMissingBean
	@Bean
	public LeaderElection leaderElection() {
		return new ApplicationLeaderElection();
	}

	@ConditionalOnMissingBean
	@Bean
	public LeaderRecovery leaderRecovery() {
		return new DefaultLeaderRecovery();
	}

}
