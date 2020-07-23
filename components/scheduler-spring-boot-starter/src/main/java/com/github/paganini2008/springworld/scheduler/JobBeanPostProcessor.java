package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 
 * JobBeanPostProcessor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Component
public class JobBeanPostProcessor implements BeanPostProcessor {

	@Autowired
	private JobManager jobManager;

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Job) {
			Job job = (Job) bean;
			jobManager.addJob(job);
			jobManager.schedule(job, job.getAttachment());
		}
		return bean;
	}

}
