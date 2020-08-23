package com.github.paganini2008.springworld.myjob;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
				scheduleManager.schedule(job);
			} catch (Exception e) {
				throw new BeanInitializationException(e.getMessage(), e);
			}
		}
		if (bean instanceof JobListener) {
			JobListener jobListener = (JobListener) bean;
			jobExecutor.addJobListener(jobListener);
			if (targetJobExecutor != null) {
				targetJobExecutor.addJobListener(jobListener);
			}
		}
		return bean;
	}

}
