package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastEventListener;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

/**
 * 
 * ConsistencyRequestCommitmentRequest
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ConsistencyRequestCommitmentRequest implements ClusterMulticastEventListener {

	@Autowired
	private ConsistencyRequestSerialCache requestSerialCache;

	@Autowired
	private InstanceId clusterId;

	@Autowired
	private ClusterMulticastGroup contextMulticastGroup;

	@Override
	public void onMessage(String instanceId, Object message) {
		ConsistencyRequest request = (ConsistencyRequest) message;
		String name = request.getName();
		long round = request.getRound();
		long serial = request.getSerial();
		long maxSerial = requestSerialCache.getSerial(name, round);
		if (serial >= maxSerial) {
			contextMulticastGroup.send(instanceId, ConsistencyRequest.COMMITMENT_RESPONSE, request.ack(clusterId.get(), true));
		} else {
			contextMulticastGroup.send(instanceId, ConsistencyRequest.COMMITMENT_RESPONSE, request.ack(clusterId.get(), false));
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.COMMITMENT_REQUEST;
	}

}
