package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 
 * ClusterMulticastEventListenerBeanProcessor
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Component
@ConditionalOnProperty(value = "spring.application.cluster.multicast.enabled", havingValue = "true")
public class ClusterMulticastEventListenerBeanProcessor implements BeanPostProcessor {

	@Autowired
	private ClusterMulticastEventListenerContainer eventListenerContainer;

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ClusterMulticastEventListener) {
			eventListenerContainer.addListener((ClusterMulticastEventListener) bean);
		}
		return bean;
	}

}
