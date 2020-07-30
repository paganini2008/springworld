package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

/**
 * 
 * LoadBalancedJobExecutor
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class LoadBalancedJobExecutor extends JobTemplate implements JobExecutor {

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private JobStore jobStore;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Override
	public void execute(Job job, Object arg) {
		runJob(job, arg);
	}

	@Override
	protected final void runJob(Job job, Object arg) {
		if (!isRunning(job)) {
			return;
		}
		final String topic = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:loadbalance";
		clusterMulticastGroup.unicast(topic, new JobParameter(job.getSignature(), arg));
	}

	@Override
	public boolean isRunning(Job job) {
		try {
			return jobStore.getJobRuntime(job).getJobState() == JobState.RUNNING;
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

}
