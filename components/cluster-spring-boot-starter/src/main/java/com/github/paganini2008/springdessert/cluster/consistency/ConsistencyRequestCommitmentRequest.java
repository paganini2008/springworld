package com.github.paganini2008.springdessert.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastGroup;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMessageListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestCommitmentRequest
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestCommitmentRequest implements ApplicationMessageListener {

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ConsistencyRequestSerialCache requestSerialCache;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private ApplicationMulticastGroup multicastGroup;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		final ConsistencyRequest request = (ConsistencyRequest) message;
		final String name = request.getName();
		if (request.getRound() != requestRound.currentRound(name)) {
			if (log.isTraceEnabled()) {
				log.trace("This round of proposal '{}' has been finished.", name);
			}
			return;
		}

		String anotherInstanceId = applicationInfo.getId();
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + anotherInstanceId + ", " + request);
		}
		long round = request.getRound();
		long serial = request.getSerial();
		long maxSerial = requestSerialCache.getSerial(name, round);
		if (serial >= maxSerial) {
			requestSerialCache.setValue(name, round, serial, request.getValue());
			multicastGroup.send(anotherInstanceId, ConsistencyRequest.COMMITMENT_OPERATION_RESPONSE,
					request.ack(instanceId.getApplicationInfo(), true));
		} else {
			multicastGroup.send(anotherInstanceId, ConsistencyRequest.COMMITMENT_OPERATION_RESPONSE,
					request.ack(instanceId.getApplicationInfo(), false));
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.COMMITMENT_OPERATION_REQUEST;
	}

}
