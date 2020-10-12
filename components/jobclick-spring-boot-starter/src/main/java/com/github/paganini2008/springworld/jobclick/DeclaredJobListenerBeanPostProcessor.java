package com.github.paganini2008.springworld.jobclick;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 
 * DeclaredJobListenerBeanPostProcessor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class DeclaredJobListenerBeanPostProcessor implements BeanPostProcessor {

	@Qualifier(BeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Qualifier(BeanNames.TARGET_JOB_EXECUTOR)
	@Autowired(required = false)
	private JobExecutor targetJobExecutor;

	@Autowired
	private LifeCycleListenerContainer lifeCycleListenerContainer;

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof LifeCycleListener) {
			LifeCycleListener listener = (LifeCycleListener) bean;
			lifeCycleListenerContainer.addListener(listener);
		}

		if (bean instanceof JobRuntimeListener) {
			JobRuntimeListener listener = (JobRuntimeListener) bean;
			if (bean instanceof JobDefinition) {
				JobKey jobKey = JobKey.of((JobDefinition) bean);
				jobExecutor.addListener(jobKey, listener);
				if (targetJobExecutor != null) {
					targetJobExecutor.addListener(jobKey, listener);
				}
			} else {
				jobExecutor.addListener(null, listener);
				if (targetJobExecutor != null) {
					targetJobExecutor.addListener(null, listener);
				}
			}

		}
		return bean;
	}

}
