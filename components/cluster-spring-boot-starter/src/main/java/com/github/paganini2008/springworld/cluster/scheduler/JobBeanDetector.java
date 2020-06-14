package com.github.paganini2008.springworld.cluster.scheduler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 
 * JobBeanDetector
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobBeanDetector implements BeanPostProcessor {

	@Autowired
	private JobManager jobManager;

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Job) {
			jobManager.addJob((Job) bean);
			jobManager.schedule((Job) bean);
		}
		return bean;
	}

}
