package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.proxy.JdkProxyFactory;
import com.github.paganini2008.devtools.proxy.ProxyFactory;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

/**
 * 
 * ServerModeJobBeanLoader
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ServerModeJobBeanLoader implements JobBeanLoader {

	@Autowired
	private JobManager jobManager;

	private final ProxyFactory proxyFactory = new JdkProxyFactory();

	@Override
	public Job loadJobBean(JobKey jobKey) throws Exception {
		Job job = ApplicationContextUtils.autowireBean(new RemoteJobBeanProxy(jobKey));
		JobTrigger trigger = jobManager.getJobTrigger(job);
		TriggerDescription triggerDescription = trigger.getTriggerDescription();
		switch (trigger.getTriggerType()) {
		case CRON:
			return (Job) proxyFactory.getProxy(job, JobAspectProvider.grantCronJob(triggerDescription), CronJob.class);
		case PERIODIC:
			return (Job) proxyFactory.getProxy(job, JobAspectProvider.grantPeriodicJob(triggerDescription), PeriodicJob.class);
		case SERIAL:
			return (Job) proxyFactory.getProxy(job, JobAspectProvider.grantSerialJob(triggerDescription), SerialJob.class);
		}
		throw new UnsupportedOperationException("Unknown trigger type: " + trigger.getTriggerType().name());
	}

}
