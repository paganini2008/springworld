package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestTimeoutRequest
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestTimeoutRequest implements ClusterMessageListener, ApplicationContextAware {

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
		String anotherInstanceId = applicationInfo.getId();
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + anotherInstanceId + ", " + message);
		}
		ConsistencyRequest request = (ConsistencyRequest) message;
		if (request.getRound() == requestRound.currentRound(request.getName())) {
			applicationContext.publishEvent(new ConsistencyRequestConfirmationEvent(request, applicationInfo, false));
			clusterMulticastGroup.send(anotherInstanceId, ConsistencyRequest.TIMEOUT_OPERATION_RESPONSE, request);
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.TIMEOUT_OPERATION_REQUEST;
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}