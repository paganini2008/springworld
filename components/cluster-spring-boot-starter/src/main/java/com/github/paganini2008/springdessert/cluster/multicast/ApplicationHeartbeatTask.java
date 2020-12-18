package com.github.paganini2008.springdessert.cluster.multicast;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.http.DirectRoutingAllocator;
import com.github.paganini2008.springdessert.cluster.http.Request;
import com.github.paganini2008.springdessert.cluster.http.RequestTemplate;
import com.github.paganini2008.springdessert.cluster.http.RestClientPerformer;
import com.github.paganini2008.springdessert.cluster.http.RetryTemplateFactory;
import com.github.paganini2008.springdessert.cluster.http.SimpleRequest;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastEvent.MulticastEventType;
import com.github.paganini2008.springdessert.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationHeartbeatTask
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class ApplicationHeartbeatTask implements Runnable {

	private static final int MINIMUM_INTERRUPTED_COUNT = 3;
	public static final String APPLICATION_PING_PATH = "/application/cluster/ping";

	private final int interruptedCount;
	private final ApplicationInfo applicationInfo;

	ApplicationHeartbeatTask(ApplicationInfo applicationInfo, RestClientPerformer restClientPerformer,
			RetryTemplateFactory retryTemplateFactory, ThreadPoolTaskExecutor taskExecutor,
			ApplicationMulticastGroup applicationMulticastGroup, int interruptedCount) {
		this.applicationInfo = applicationInfo;
		this.requestTemplate = new RequestTemplate(new DirectRoutingAllocator(), restClientPerformer, retryTemplateFactory, taskExecutor);
		this.applicationMulticastGroup = applicationMulticastGroup;
		this.interruptedCount = Integer.max(MINIMUM_INTERRUPTED_COUNT, interruptedCount);
	}

	private final RequestTemplate requestTemplate;
	private final AtomicInteger errorCounter = new AtomicInteger();
	private final ApplicationMulticastGroup applicationMulticastGroup;

	@Override
	public void run() {
		try {
			SimpleRequest request = SimpleRequest.getRequest(APPLICATION_PING_PATH);
			request.setAttribute(Request.MAX_TIMEOUT, 60);
			ResponseEntity<ApplicationInfo> responseEntity = requestTemplate.sendRequest(applicationInfo.getApplicationContextPath(),
					request, ApplicationInfo.class);
			if (log.isTraceEnabled()) {
				log.trace("Heartbeat info: " + responseEntity.getBody().toString());
			}
			errorCounter.set(0);
		} catch (RestClientException e) {
			log.error(e.getMessage(), e);
			if (errorCounter.incrementAndGet() > interruptedCount) {
				applicationMulticastGroup.removeCandidate(applicationInfo);
				log.warn(
						"Failed to keep heartbeating from inactive application '{}' after trying {} times. ApplicationMulticastEvent will be sent.",
						applicationInfo, interruptedCount);
				ApplicationContextUtils.publishEvent(new ApplicationMulticastEvent(ApplicationContextUtils.getApplicationContext(),
						applicationInfo, MulticastEventType.ON_INACTIVE));
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
	}

}
