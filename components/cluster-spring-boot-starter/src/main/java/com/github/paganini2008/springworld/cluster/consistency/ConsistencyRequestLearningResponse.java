package com.github.paganini2008.springworld.cluster.consistency;

import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastEventListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestLearningResponse
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestLearningResponse implements ClusterMulticastEventListener {

	@Override
	public void onMessage(String instanceId, Object message) {
		ConsistencyRequest request = (ConsistencyRequest) message;
		log.info(getTopic() + "\t" + request.getName() + "\t" + request.getValue());
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.LEARNING_RESPONSE;
	}

}
