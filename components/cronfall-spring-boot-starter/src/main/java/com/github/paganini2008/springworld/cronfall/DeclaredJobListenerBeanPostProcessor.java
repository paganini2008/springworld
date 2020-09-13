package com.github.paganini2008.springworld.cronfall;

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
	private LifecycleListenerContainer lifecycleListenerContainer;

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof LifecycleListener) {
			LifecycleListener listener = (LifecycleListener) bean;
			lifecycleListenerContainer.addListener(listener);
		}

		if (bean instanceof JobRuntimeListener) {
			JobRuntimeListener jobListener = (JobRuntimeListener) bean;
			jobExecutor.addListener(jobListener);
			if (targetJobExecutor != null) {
				targetJobExecutor.addListener(jobListener);
			}
		}
		return bean;
	}

}
