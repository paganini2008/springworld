package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

/**
 * 
 * JobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobAdmin implements ClusterMessageListener {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	public void addJob(JobParam jobParam) {
		clusterMulticastGroup.unicast(jobParam.getJobKey().getGroupName(), getTopic(), jobParam);
	}

	public void deleteJob(JobKey jobKey) {
		Job job;
		try {
			job = jobBeanLoader.loadJobBean(jobKey);
			jobManager.deleteJob(job);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	public boolean hasJob(JobKey jobKey) {
		Job job;
		try {
			job = jobBeanLoader.loadJobBean(jobKey);
			return jobManager.hasJob(job);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
		final JobParam jobParam = (JobParam) message;
		Job job;
		try {
			job = jobBeanLoader.loadJobBean(jobParam.getJobKey());
			jobManager.addJob(job, (String) jobParam.getAttachment());
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public String getTopic() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:add";
	}

}
