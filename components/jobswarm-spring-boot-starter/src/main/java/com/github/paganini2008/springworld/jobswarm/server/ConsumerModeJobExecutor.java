package com.github.paganini2008.springworld.jobswarm.server;

import java.util.Date;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.jobswarm.DependencyType;
import com.github.paganini2008.springworld.jobswarm.Job;
import com.github.paganini2008.springworld.jobswarm.SerialJobDependencyScheduler;
import com.github.paganini2008.springworld.jobswarm.JobException;
import com.github.paganini2008.springworld.jobswarm.JobExecutor;
import com.github.paganini2008.springworld.jobswarm.JobKey;
import com.github.paganini2008.springworld.jobswarm.JobLoggerFactory;
import com.github.paganini2008.springworld.jobswarm.JobManager;
import com.github.paganini2008.springworld.jobswarm.JobRuntimeListenerContainer;
import com.github.paganini2008.springworld.jobswarm.JobTemplate;
import com.github.paganini2008.springworld.jobswarm.LogManager;
import com.github.paganini2008.springworld.jobswarm.RetryPolicy;
import com.github.paganini2008.springworld.jobswarm.RunningState;
import com.github.paganini2008.springworld.jobswarm.StopWatch;
import com.github.paganini2008.springworld.jobswarm.TraceIdGenerator;

/**
 * 
 * ConsumerModeJobExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsumerModeJobExecutor extends JobTemplate implements JobExecutor {

	@Autowired
	private SerialJobDependencyScheduler jobDependencyScheduler;

	@Autowired
	private StopWatch stopWatch;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private LogManager logManager;

	@Autowired
	private RetryPolicy retryPolicy;

	@Autowired
	private TraceIdGenerator idGenerator;

	@Autowired
	private JobRuntimeListenerContainer jobRuntimeListenerContainer;

	@Override
	protected long getTraceId(JobKey jobKey) {
		long traceId = idGenerator.generateTraceId(jobKey);
		setCustomizedLog(JobLoggerFactory.getLogger(log, traceId, jobKey, logManager));
		return traceId;
	}

	@Override
	public void execute(Job job, Object attachment, int retries) {
		runJob(job, attachment, retries);
	}

	@Override
	protected boolean isScheduling(JobKey jobKey, Job job) {
		return true;
	}

	@Override
	protected void notifyDependants(JobKey jobKey, Job job, Object result) {
		try {
			if (jobManager.hasRelations(jobKey, DependencyType.SERIAL)) {
				jobDependencyScheduler.notifyDependants(jobKey, result);
			}
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	protected Object retry(JobKey jobKey, Job job, Object attachment, Throwable reason, int retries, Logger log) throws Throwable {
		return retryPolicy.retryIfNecessary(jobKey, job, attachment, reason, retries, log);
	}

	@Override
	protected void afterRun(long traceId, JobKey jobKey, Job job, Object attachment, Date startDate, RunningState runningState,
			Object result, Throwable reason, int retries) {
		super.afterRun(traceId, jobKey, job, attachment, startDate, runningState, result, reason, retries);
		jobRuntimeListenerContainer.afterRun(traceId, jobKey, job, attachment, startDate, runningState, result, reason, retries);
		stopWatch.finishJob(traceId, jobKey, startDate, runningState, retries);
	}
}
