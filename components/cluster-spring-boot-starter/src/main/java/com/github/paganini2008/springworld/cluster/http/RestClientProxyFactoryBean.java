package com.github.paganini2008.springworld.cluster.http;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.proxy.ProxyFactory;
import com.github.paganini2008.springworld.cluster.LeaderContext;
import com.github.paganini2008.springworld.cluster.utils.LazilyAutowiredBeanInspector;
import com.github.paganini2008.springworld.cluster.utils.RetryTemplateFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RestClientProxyFactoryBean
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class RestClientProxyFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {

	private final Class<T> interfaceClass;

	public RestClientProxyFactoryBean(Class<T> interfaceClass) {
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
	private RetryTemplateFactory retryTemplateFactory;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	private LeaderContext leaderContext;

	@Autowired
	private RequestInterceptorContainer requestInterceptorContainer;

	@Autowired
	private LazilyAutowiredBeanInspector lazilyAutowiredBeanInspector;

	private ConfigurableBeanFactory beanFactory;

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		final RestClient restClient = interfaceClass.getAnnotation(RestClient.class);
		String provider = beanFactory.resolveEmbeddedValue(restClient.provider());
		int retries = restClient.retries();
		int timeout = restClient.timeout();
		Class<?> fallbackClass = restClient.fallback();
		RequestProcessor requestProcessor = new DefaultRequestProcessor(provider, defaultHttpHeaders, routingPolicy, restTemplate,
				retryTemplateFactory, taskExecutor);
		Object fallback = fallbackClass != Void.class || fallbackClass != void.class ? BeanUtils.instantiate(fallbackClass) : null;
		if (fallback != null) {
			lazilyAutowiredBeanInspector.autowireLazily(fallback);
		}
		log.info("Create rest client for provider: {}, retries:{}, timeout: {}", provider, retries, timeout);
		return (T) ProxyFactory.getDefault().getProxy(fallback,
				new RestClientBeanAspect(leaderContext, requestProcessor, retries, timeout, requestInterceptorContainer),
				new Class<?>[] { interfaceClass });
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

}
