package com.github.paganini2008.springworld.cronfall;

import java.util.Date;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.cronfall.model.JobParam;

/**
 * 
 * EmbeddedModeLoadBalancer
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class EmbeddedModeLoadBalancer extends JobTemplate implements JobExecutor {

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired
	private StopWatch stopWatch;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private TraceIdGenerator idGenerator;

	@Override
	protected long getTraceId(JobKey jobKey) {
		return idGenerator.generateTraceId(jobKey);
	}

	@Override
	public void execute(Job job, Object attachment, int retries) {
		runJob(job, attachment, retries);
	}

	@Override
	protected void beforeRun(long traceId, JobKey jobKey, Job job, Date startTime) {
		super.beforeRun(traceId, jobKey, job, startTime);
		stopWatch.startJob(traceId, jobKey, startTime);
	}

	@Override
	protected final RunningState doRun(JobKey jobKey, Job job, Object attachment, int retries, Logger log) {
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
		try {
			return jobManager.hasJobState(jobKey, JobState.SCHEDULING);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	protected void cancel(JobKey jobKey, Job job, RunningState runningState, String msg, Throwable reason) {
		try {
			scheduleManager.unscheduleJob(jobKey);
			jobManager.deleteJob(jobKey);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}

		if (StringUtils.isNotBlank(msg)) {
			log.info(msg);
		}
		if (reason != null) {
			log.error(reason.getMessage(), reason);
		}
	}

}
