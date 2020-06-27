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
 * ConsistencyRequestLearningRequest
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestLearningRequest implements ClusterMessageListener, ApplicationContextAware {

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ConsistencyRequestSerial requestSerial;

	@Autowired
	private ConsistencyRequestSerialCache requestSerialCache;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private Court court;

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
			log.trace(getTopic() + " " + anotherInstanceId + ", " + request);
		}
		if (log.isDebugEnabled()) {
			log.debug("Selected ConsistencyRequest: " + request);
		}
		clean(name);
		court.completeProposal(name);

		applicationContext.publishEvent(new ConsistencyRequestConfirmationEvent(request, applicationInfo, true));
		clusterMulticastGroup.send(anotherInstanceId, ConsistencyRequest.LEARNING_OPERATION_RESPONSE, request);

	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.LEARNING_OPERATION_REQUEST;
	}

	private void clean(String name) {
		requestRound.clean(name);
		requestSerial.clean(name);
		requestSerialCache.clean(name);
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
