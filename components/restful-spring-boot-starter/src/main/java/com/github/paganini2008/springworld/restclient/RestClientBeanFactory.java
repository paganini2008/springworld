package com.github.paganini2008.springworld.restclient;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.proxy.JdkProxyFactory;
import com.github.paganini2008.devtools.proxy.ProxyFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RestClientBeanFactory
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class RestClientBeanFactory<T> implements FactoryBean<T> {

	private static final ProxyFactory proxyFactory = new JdkProxyFactory();
	private final Class<T> interfaceClass;

	public RestClientBeanFactory(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	@Qualifier("defaultHttpHeaders")
	@Autowired(required = false)
	private HttpHeaders defaultHttpHeaders;

	@Autowired
	private RoutingPolicy routingPolicy;

	@Autowired
	private EnhancedRestTemplate restTemplate;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	private RequestInterceptorContainer requestInterceptorContainer;

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		RestClient restClient = interfaceClass.getAnnotation(RestClient.class);
		String provider = restClient.provider();
		int retries = restClient.retries();
		int timeout = restClient.timeout();
		RequestProcessor requestProcessor = new DefaultRequestProcessor(provider, retries, timeout, defaultHttpHeaders, routingPolicy,
				restTemplate, taskExecutor);
		Object fallback = BeanUtils.instantiate(restClient.fallback());
		log.info("Create rest client for provider: {}, retries:{}, timeout: {}", provider, retries, timeout);
		return (T) proxyFactory.getProxy(getObjectType().cast(fallback),
				new RestClientBeanAspect(requestProcessor, requestInterceptorContainer), new Class<?>[] { interfaceClass });
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

}
