package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestPreparationRequest
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestPreparationRequest implements ClusterMessageListener {

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ConsistencyRequestSerialCache requestSerialCache;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
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
			log.trace(getTopic() + " " + anotherInstanceId + ", " + message);
		}
		long round = request.getRound();
		long serial = request.getSerial();
		long maxSerial = requestSerialCache.getSerial(name, round);
		if (serial > maxSerial) {
			Object preValue = requestSerialCache.setValue(name, round, serial, request.getValue());
			if (preValue != null) {
				System.out.println("preValue: " + preValue);
				request.setValue(preValue);
			}
			clusterMulticastGroup.send(anotherInstanceId, ConsistencyRequest.PREPARATION_OPERATION_RESPONSE,
					request.ack(instanceId.getApplicationInfo(), true));
		} else {
			clusterMulticastGroup.send(anotherInstanceId, ConsistencyRequest.PREPARATION_OPERATION_RESPONSE,
					request.ack(instanceId.getApplicationInfo(), false));
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.PREPARATION_OPERATION_REQUEST;
	}

}
