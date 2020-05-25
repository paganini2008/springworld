package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationClusterFollowerEvent;
import com.github.paganini2008.springworld.cluster.ApplicationClusterNewLeaderEvent;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.LeaderElection;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyLeaderElection
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyLeaderElection implements LeaderElection, ApplicationContextAware, ApplicationListener<ConsistencyOperationResult> {

	private ApplicationContext applicationContext;

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private InstanceId instanceId;

	@Override
	public void lookupLeader(ApplicationEvent applicationEvent) {
		log.info("Lookup leader for application cluster '{}'", applicationName);
		ConsistencyOperationResult result = (ConsistencyOperationResult) applicationEvent;
		String newLeaderId = (String) result.getValue();
		if (instanceId.get().equals(newLeaderId)) {
			applicationContext.publishEvent(new ApplicationClusterNewLeaderEvent(applicationContext));
			log.info("You are the leader of application cluster '{}'. Implement ApplicationListener to listen event type {}",
					applicationName, ApplicationClusterNewLeaderEvent.class.getName());
		} else {
			applicationContext.publishEvent(new ApplicationClusterFollowerEvent(applicationContext, newLeaderId));
			log.info("You are the follower of application cluster '{}'. Implement ApplicationListener to listen the event type {}",
					applicationName, ApplicationClusterFollowerEvent.class.getName());
		}
		instanceId.setLeaderId(newLeaderId);
	}

	@Override
	public void onApplicationEvent(ConsistencyOperationResult result) {
		final String leaderIdentify = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":leader";
		if (leaderIdentify.equals(result.getName())) {
			lookupLeader(result);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
