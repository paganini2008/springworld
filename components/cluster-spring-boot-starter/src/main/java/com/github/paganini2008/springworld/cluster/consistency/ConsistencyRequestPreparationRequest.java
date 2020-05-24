package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.ClusterId;
import com.github.paganini2008.springworld.cluster.multicast.ContextMulticastEventHandler;
import com.github.paganini2008.springworld.cluster.multicast.ContextMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestPreparationRequest
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestPreparationRequest implements ContextMulticastEventHandler {

	@Autowired
	private ConsistencyRequestSerialCache requestSerialCache;

	@Autowired
	private ClusterId clusterId;

	@Autowired
	private ContextMulticastGroup contextMulticastGroup;

	@Override
	public void onMessage(String instanceId, Object message) {
		ConsistencyRequest request = (ConsistencyRequest) message;
		String name = request.getName();
		long round = request.getRound();
		long serial = request.getSerial();
		long maxSerial = requestSerialCache.getSerial(name, round);
		if (serial >= maxSerial) {
			requestSerialCache.setSerial(name, round, serial);
			contextMulticastGroup.send(instanceId, ConsistencyRequest.PREPARATION_RESPONSE, request.ack(clusterId.get(), true));
		} else {
			contextMulticastGroup.send(instanceId, ConsistencyRequest.PREPARATION_RESPONSE, request.ack(clusterId.get(), false));
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.PREPARATION_REQUEST;
	}

}
