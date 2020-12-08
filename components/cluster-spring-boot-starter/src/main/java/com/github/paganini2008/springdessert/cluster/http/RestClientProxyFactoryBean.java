package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.paganini2008.devtools.proxy.ProxyFactory;
import com.github.paganini2008.springdessert.cluster.LeaderContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RestClientProxyFactoryBean
 * 
 * @author Jimmy Hoff
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
	private RoutingAllocator routingAllocator;

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

	private ConfigurableBeanFactory beanFactory;

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		final RestClient restClient = interfaceClass.getAnnotation(RestClient.class);
		final String provider = beanFactory.resolveEmbeddedValue(restClient.provider());
		RequestProcessor requestProcessor = new DefaultRequestProcessor(provider, defaultHttpHeaders, routingAllocator, restTemplate,
				retryTemplateFactory, taskExecutor);
		log.info("Create RestClient for provider: {}", provider);
		return (T) ProxyFactory.getDefault().getProxy(null,
				new RestClientBeanAspect(restClient, leaderContext, requestProcessor, requestInterceptorContainer),
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
