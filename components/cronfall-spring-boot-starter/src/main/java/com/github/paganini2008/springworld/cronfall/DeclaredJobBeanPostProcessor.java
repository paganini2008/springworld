package com.github.paganini2008.springworld.cronfall;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 
 * DeclaredJobBeanPostProcessor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class DeclaredJobBeanPostProcessor implements BeanPostProcessor {

	@Autowired
	private JobManager jobManager;

	@Autowired
	private ScheduleManager scheduleManager;

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Job) {
			Job job = (Job) bean;
			try {
				jobManager.persistJob(job, null);
				scheduleManager.schedule(job);
			} catch (Exception e) {
				throw new BeanInitializationException(e.getMessage(), e);
			}
		}
		return bean;
	}

}
