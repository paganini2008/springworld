package com.github.paganini2008.springworld.crontab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.crontab.model.JobParam;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * FailoverRetryPolicy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class FailoverRetryPolicy implements RetryPolicy {

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private JobManager jobManager;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Override
	public Object retryIfNecessary(JobKey jobKey, Job job, Object attachment, Throwable reason, int retries) throws Throwable {
		if (clusterMulticastGroup.countOfChannel(jobKey.getGroupName()) > 0) {
			final String topic = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:loadbalance";
			clusterMulticastGroup.unicast(jobKey.getGroupName(), topic, new JobParam(jobKey, attachment, retries));
		} else {
			try {
				jobManager.setJobState(jobKey, JobState.SCHEDULING);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		throw reason;
	}

}
