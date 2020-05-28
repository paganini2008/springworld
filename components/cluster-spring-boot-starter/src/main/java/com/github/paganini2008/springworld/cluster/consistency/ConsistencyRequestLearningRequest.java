package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestLearningRequest
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestLearningRequest implements ClusterMessageListener, ApplicationContextAware {

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private Court court;

	@Override
	public void onMessage(String anotherInstanceId, Object message) {
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + anotherInstanceId + ", " + message);
		}
		ConsistencyRequest request = (ConsistencyRequest) message;
		if (request.getRound() == requestRound.currentRound(request.getName())) {
			if (log.isDebugEnabled()) {
				log.debug("Selected ConsistencyRequest: " + request);
			}
			court.completeProposal(request.getName());
			applicationContext.publishEvent(new ConsistencyRequestConfirmationEvent(request, anotherInstanceId, true));
			clusterMulticastGroup.send(anotherInstanceId, ConsistencyRequest.LEARNING_OPERATION_RESPONSE, request);
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.LEARNING_OPERATION_REQUEST;
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
