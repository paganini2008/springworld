package com.github.paganini2008.springdessert.cluster.multicast;

import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.http.DirectRoutingAllocator;
import com.github.paganini2008.springdessert.cluster.http.ForwardedRequest;
import com.github.paganini2008.springdessert.cluster.http.RequestInterceptorContainer;
import com.github.paganini2008.springdessert.cluster.http.RequestTemplate;
import com.github.paganini2008.springdessert.cluster.http.RestClientPerformer;
import com.github.paganini2008.springdessert.cluster.http.RetryTemplateFactory;
import com.github.paganini2008.springdessert.cluster.http.StatisticIndicator;
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

	private static final int DEFAULT_MINUMUN_TIMEOUT = 5;
	public static final String APPLICATION_PING_PATH = "/application/cluster/ping";

	private final int timeout;
	private final ApplicationInfo applicationInfo;

	ApplicationHeartbeatTask(ApplicationInfo applicationInfo, RestClientPerformer restClientPerformer,
			RetryTemplateFactory retryTemplateFactory, ThreadPoolTaskExecutor taskExecutor,
			RequestInterceptorContainer requestInterceptorContainer, StatisticIndicator statisticIndicator,
			ApplicationMulticastGroup applicationMulticastGroup, int timeout) {
		this.applicationInfo = applicationInfo;
		this.requestTemplate = new RequestTemplate(new DirectRoutingAllocator(), restClientPerformer, retryTemplateFactory, taskExecutor,
				requestInterceptorContainer, statisticIndicator);
		this.applicationMulticastGroup = applicationMulticastGroup;
		this.timeout = Integer.max(DEFAULT_MINUMUN_TIMEOUT, timeout);
	}

	private final RequestTemplate requestTemplate;
	private final ApplicationMulticastGroup applicationMulticastGroup;
	private volatile long timestamp;

	@Override
	public void run() {
		try {
			ForwardedRequest request = ForwardedRequest.getRequest(APPLICATION_PING_PATH);
			request.setTimeout(60);
			ResponseEntity<ApplicationInfo> responseEntity = requestTemplate.sendRequest(applicationInfo.getApplicationContextPath(),
					request, ApplicationInfo.class);
			if (log.isTraceEnabled()) {
				log.trace("Heartbeat info: " + responseEntity.getBody().toString());
			}
			timestamp = System.currentTimeMillis();
		} catch (RestClientException e) {
			log.error(e.getMessage(), e);
			if (System.currentTimeMillis() - timestamp > DateUtils.convertToMillis(timeout, TimeUnit.SECONDS)) {
				applicationMulticastGroup.removeCandidate(applicationInfo);
				log.warn(
						"Failed to keep heartbeating from inactive application '{}' because of timeout settings: {}. ApplicationMulticastEvent will be sent.",
						applicationInfo, timeout);
				ApplicationContextUtils.publishEvent(new ApplicationMulticastEvent(ApplicationContextUtils.getApplicationContext(),
						applicationInfo, MulticastEventType.ON_INACTIVE));
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
	}

}
