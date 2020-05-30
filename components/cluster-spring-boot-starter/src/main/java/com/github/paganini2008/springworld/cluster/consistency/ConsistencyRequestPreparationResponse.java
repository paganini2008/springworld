package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestPreparationResponse
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestPreparationResponse implements ClusterMessageListener {

	@Autowired
	private Court court;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + applicationInfo.getId() + ", " + message);
		}
		ConsistencyResponse response = (ConsistencyResponse) message;
		if (response.isAcceptable()) {
			court.canCommit(response);
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.PREPARATION_OPERATION_RESPONSE;
	}

}
