package com.github.paganini2008.springworld.cluster.consistency;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;
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
public class ConsistencyRequestLearningResponse implements ClusterMessageListener, ApplicationContextAware {

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
		String anotherInstanceId = applicationInfo.getId();
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + anotherInstanceId + ", " + message);
		}
		ConsistencyRequest request = (ConsistencyRequest) message;
		if (log.isDebugEnabled()) {
			log.debug("InstanceId '" + anotherInstanceId + "' learns " + request);
		}
		applicationContext.publishEvent(new ConsistencyRequestCompletionEvent(request, applicationInfo));
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.LEARNING_OPERATION_RESPONSE;
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
