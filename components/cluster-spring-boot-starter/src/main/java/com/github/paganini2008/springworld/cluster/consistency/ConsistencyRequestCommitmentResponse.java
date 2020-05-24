package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastEventListener;

/**
 * 
 * ConsistencyRequestCommitmentResponse
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistencyRequestCommitmentResponse implements ClusterMulticastEventListener {

	@Autowired
	private ConsistencyRequestContext context;

	@Override
	public void onMessage(String instanceId, Object message) {
		ConsistencyResponse response = (ConsistencyResponse) message;
		if (response.isAcceptable()) {
			context.canLearn(response);
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.COMMITMENT_RESPONSE;
	}

}
