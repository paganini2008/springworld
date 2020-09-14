package com.github.paganini2008.springworld.cronkeeper;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 
 * NotScheduledJobBeanPostProcessor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class NotScheduledJobBeanPostProcessor implements BeanPostProcessor {

	@Autowired
	private JobManager jobManager;

	@Qualifier(BeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Qualifier(BeanNames.TARGET_JOB_EXECUTOR)
	@Autowired(required = false)
	private JobExecutor targetJobExecutor;

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Job) {
			Job job = (Job) bean;
			try {
				jobManager.persistJob(job, null);
			} catch (Exception e) {
				throw new BeanInitializationException(e.getMessage(), e);
			}
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
