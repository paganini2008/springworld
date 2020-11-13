package com.github.paganini2008.springworld.cluster.http;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.paganini2008.devtools.proxy.ProxyFactory;
import com.github.paganini2008.springworld.cluster.LeaderRoutingPolicy;

/**
 * 
 * LeaderServiceFactoryBean
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class LeaderServiceFactoryBean implements FactoryBean<LeaderService> {

	@Autowired
	private EnhancedRestTemplate restTemplate;

	@Autowired
	private RetryTemplateFactory retryTemplateFactory;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	private RequestInterceptorContainer requestInterceptorContainer;

	@Override
	public LeaderService getObject() throws Exception {
		final RequestProcessor requestProcessor = new DefaultRequestProcessor(RoutingPolicy.LEADER_ALIAS, 3, 60, null,
				new LeaderRoutingPolicy(), restTemplate, retryTemplateFactory, taskExecutor);
		return (LeaderService) ProxyFactory.getDefault().getProxy(null,
				new RestClientBeanAspect(requestProcessor, requestInterceptorContainer), new Class<?>[] { LeaderService.class });
	}

	@Override
	public Class<?> getObjectType() {
		return LeaderService.class;
	}

}
