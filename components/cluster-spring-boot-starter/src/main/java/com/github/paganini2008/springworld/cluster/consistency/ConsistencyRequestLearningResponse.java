package com.github.paganini2008.springworld.cluster.consistency;

import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestLearningResponse
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestLearningResponse implements ClusterMessageListener {

	@Override
	public void onMessage(String instanceId, Object message) {
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + instanceId + ", " + message);
		}
		ConsistencyRequest request = (ConsistencyRequest) message;
		if (log.isDebugEnabled()) {
			log.debug("InstanceId '" + instanceId + "' learns " + request);
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.LEARNING_OPERATION_RESPONSE;
	}

}
