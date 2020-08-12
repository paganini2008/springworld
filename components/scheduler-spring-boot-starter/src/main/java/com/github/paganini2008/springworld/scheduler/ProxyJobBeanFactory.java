package com.github.paganini2008.springworld.scheduler;

import com.github.paganini2008.devtools.proxy.JdkProxyFactory;
import com.github.paganini2008.devtools.proxy.ProxyFactory;

/**
 * 
 * ProxyJobBeanFactory
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ProxyJobBeanFactory implements JobBeanFactory {

	private final ProxyFactory proxyFactory = new JdkProxyFactory();

	@Override
	public Job newJobBean(JobConfig jobConfig) {
		ProxyJobBean jobBean = new ProxyJobBean(jobConfig);
		switch (jobConfig.getTriggerType()) {
		case CRON:
			return (Job) proxyFactory.getProxy(jobBean, JobAspectProvider.grantCronJob(jobConfig.getTriggerDescription()), CronJob.class);
		case PERIODIC:
			return (Job) proxyFactory.getProxy(jobBean, JobAspectProvider.grantPeriodicJob(jobConfig.getTriggerDescription()),
					PeriodicJob.class);
		case SERIAL:
			return (Job) proxyFactory.getProxy(jobBean, JobAspectProvider.grantSerialJob(jobConfig.getTriggerDescription()),
					SerialJob.class);
		}
		throw new IllegalStateException();
	}

}
