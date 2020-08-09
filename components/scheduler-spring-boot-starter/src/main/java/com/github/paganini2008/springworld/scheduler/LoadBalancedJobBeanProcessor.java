package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;

/**
 * 
 * LoadBalancedJobBeanProcessor
 *
 * @author Fred Feng
 * @since 1.0
 */
public class LoadBalancedJobBeanProcessor implements ClusterMessageListener {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Qualifier("target-job-executor")
	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
		acceptJob((JobParam) message);
	}

	private void acceptJob(final JobParam jobParam) {
		Job job;
		try {
			job = jobBeanLoader.loadJobBean(jobParam.getJobKey());
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
		jobExecutor.execute(job, jobParam.getAttachment());
	}

	@Override
	public String getTopic() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:loadbalance";
	}

}
