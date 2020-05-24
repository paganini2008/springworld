package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastEventListener;
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
public class ConsistencyRequestLearningRequest implements ClusterMulticastEventListener {

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ClusterMulticastGroup contextMulticastGroup;

	@Override
	public void onMessage(String instanceId, Object message) {
		ConsistencyRequest request = (ConsistencyRequest) message;
		if (request.getRound() == requestRound.currentRound(request.getName())) {
			log.info(getTopic() + "\t" + request.getName() + "\t" + request.getValue());
			contextMulticastGroup.send(instanceId, ConsistencyRequest.LEARNING_RESPONSE, request);
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.LEARNING_REQUEST;
	}

}
