package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

/**
 * 
 * LoadBalancedJobBeanProcessor
 *
 * @author Fred Feng
 * @since 1.0
 */
public class LoadBalancedJobBeanProcessor implements ClusterMessageListener {

	@Value("${spring.application.name}")
	private String applicationName;

	@Qualifier("directJobExecutor")
	@Autowired
	private JobExecutor jobExecutor;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
		final JobParameter jobParameter = (JobParameter) message;
		final String[] data = jobParameter.getSignature().split("@", 2);
		final String jobName = data[0];
		final String jobClassName = data[1];
		Class<?> jobClass = ClassUtils.forName(jobClassName);
		if (!Job.class.isAssignableFrom(jobClass)) {
			throw new JobException("Class '" + jobClass.getName() + "' is not a implementor of Job interface.");
		}
		Job job = (Job) ApplicationContextUtils.getBean(jobName, jobClass);
		if (job == null) {
			job = (Job) ApplicationContextUtils.getBean(jobClass, bean -> {
				return ((Job) bean).getJobName().equals(jobName);
			});
		}
		if (job == null) {
			throw new JobBeanNotFoundException(jobClassName);
		}
		jobExecutor.execute(job, jobParameter.getArgument());
	}

	@Override
	public String getTopic() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + applicationName + ":scheduler:loadbalance";
	}

}
