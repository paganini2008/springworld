package com.github.paganini2008.springworld.crontab.server;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.crontab.Job;
import com.github.paganini2008.springworld.crontab.JobDependencyFuture;
import com.github.paganini2008.springworld.crontab.JobException;
import com.github.paganini2008.springworld.crontab.JobExecutor;
import com.github.paganini2008.springworld.crontab.JobFutureHolder;
import com.github.paganini2008.springworld.crontab.JobKey;
import com.github.paganini2008.springworld.crontab.JobManager;
import com.github.paganini2008.springworld.crontab.JobState;
import com.github.paganini2008.springworld.crontab.JobTemplate;
import com.github.paganini2008.springworld.crontab.RunningState;
import com.github.paganini2008.springworld.crontab.StopWatch;
import com.github.paganini2008.springworld.crontab.model.JobParam;
import com.github.paganini2008.springworld.redisplus.common.RedisUUID;

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
	private RedisUUID redisUUID;

	@Override
	protected long getTraceId(JobKey jobKey) {
		return redisUUID.createUUID().timestamp();
	}

	@Override
	public void execute(Job job, Object attachment, int retries) {
		runJob(job, attachment, retries);
	}

	@Override
	protected void beforeRun(long traceId, JobKey jobKey, Job job, Date startTime) {
		super.beforeRun(traceId, jobKey, job, startTime);
		handleIfDependentJob(traceId, jobKey, startTime);
	}

	private void handleIfDependentJob(long traceId, JobKey jobKey, Date startTime) {
		if (jobFutureHolder.get(jobKey) instanceof JobDependencyFuture) {
			stopWatch.startJob(traceId, jobKey, startTime);
		}
	}

	@Override
	protected final RunningState doRun(JobKey jobKey, Job job, Object attachment, int retries) {
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
		return RunningState.RUNNING;
	}

	@Override
	protected boolean isScheduling(JobKey jobKey, Job job) {
		return true;
	}

}
