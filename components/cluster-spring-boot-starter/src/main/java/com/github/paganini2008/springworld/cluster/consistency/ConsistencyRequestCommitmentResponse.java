package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestCommitmentResponse
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestCommitmentResponse implements ClusterMessageListener {

	@Autowired
	private ConsistencyRequestContext context;

	@Override
	public void onMessage(String instanceId, Object message) {
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + instanceId + ", " + message);
		}
		ConsistencyResponse response = (ConsistencyResponse) message;
		if (response.isAcceptable()) {
			context.canLearn(response);
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.COMMITMENT_OPERATION_RESPONSE;
	}

}
