package com.github.paganini2008.springworld.jobclick.server;

import java.util.Date;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.jobclick.Job;
import com.github.paganini2008.springworld.jobclick.JobDependencyFuture;
import com.github.paganini2008.springworld.jobclick.JobException;
import com.github.paganini2008.springworld.jobclick.JobExecutor;
import com.github.paganini2008.springworld.jobclick.JobFutureHolder;
import com.github.paganini2008.springworld.jobclick.JobKey;
import com.github.paganini2008.springworld.jobclick.JobManager;
import com.github.paganini2008.springworld.jobclick.JobRuntimeListenerContainer;
import com.github.paganini2008.springworld.jobclick.JobState;
import com.github.paganini2008.springworld.jobclick.JobTemplate;
import com.github.paganini2008.springworld.jobclick.RunningState;
import com.github.paganini2008.springworld.jobclick.StopWatch;
import com.github.paganini2008.springworld.jobclick.TraceIdGenerator;
import com.github.paganini2008.springworld.jobclick.model.JobParam;

/**
 * 
 * ConsumerModeLoadBalancer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsumerModeLoadBalancer extends JobTemplate implements JobExecutor {

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private StopWatch stopWatch;

	@Autowired
	private JobFutureHolder jobFutureHolder;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private JobRuntimeListenerContainer jobRuntimeListenerContainer;

	@Override
	protected long getTraceId(JobKey jobKey) {
		return TraceIdGenerator.NOOP.generateTraceId(jobKey);
	}

	@Override
	public void execute(Job job, Object attachment, int retries) {
		runJob(job, attachment, retries);
	}

	@Override
	protected void beforeRun(long traceId, JobKey jobKey, Job job, Object attachment, Date startDate) {
		super.beforeRun(traceId, jobKey, job, attachment, startDate);
		jobRuntimeListenerContainer.beforeRun(traceId, jobKey, job, attachment, startDate);
		handleIfDependentJob(traceId, jobKey, startDate);
	}

	private void handleIfDependentJob(long traceId, JobKey jobKey, Date startDate) {
		if (jobFutureHolder.get(jobKey) instanceof JobDependencyFuture) {
			stopWatch.startJob(traceId, jobKey, startDate);
		}
	}

	@Override
	protected final Object[] doRun(JobKey jobKey, Job job, Object attachment, int retries, Logger log) {
		if (clusterMulticastGroup.countOfChannel(jobKey.getGroupName()) > 0) {
			final String topic = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:loadbalance";
			clusterMulticastGroup.unicast(jobKey.getGroupName(), topic, new JobParam(jobKey, attachment, retries));
		} else {
			try {
				jobManager.setJobState(jobKey, JobState.SCHEDULING);
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
		}
		return new Object[] { RunningState.RUNNING, null };
	}

	@Override
	protected boolean isScheduling(JobKey jobKey, Job job) {
		return true;
	}

}
