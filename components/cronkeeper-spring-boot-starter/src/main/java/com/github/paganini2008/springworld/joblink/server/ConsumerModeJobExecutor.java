package com.github.paganini2008.springworld.joblink.server;

import java.util.Date;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.joblink.Job;
import com.github.paganini2008.springworld.joblink.JobDependencyObservable;
import com.github.paganini2008.springworld.joblink.JobException;
import com.github.paganini2008.springworld.joblink.JobExecutor;
import com.github.paganini2008.springworld.joblink.JobKey;
import com.github.paganini2008.springworld.joblink.JobLoggerFactory;
import com.github.paganini2008.springworld.joblink.JobManager;
import com.github.paganini2008.springworld.joblink.JobTemplate;
import com.github.paganini2008.springworld.joblink.LogManager;
import com.github.paganini2008.springworld.joblink.RetryPolicy;
import com.github.paganini2008.springworld.joblink.RunningState;
import com.github.paganini2008.springworld.joblink.StopWatch;
import com.github.paganini2008.springworld.joblink.TraceIdGenerator;

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
	private JobDependencyObservable jobDependencyObservable;

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
			if (jobManager.hasRelations(jobKey)) {
				jobDependencyObservable.notifyDependants(jobKey, result);
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
	protected void afterRun(long traceId, JobKey jobKey, Job job, Object attachment, Date startTime, RunningState runningState,
			Object result, Throwable reason, int retries) {
		super.afterRun(traceId, jobKey, job, attachment, startTime, runningState, result, reason, retries);
		stopWatch.finishJob(traceId, jobKey, startTime, runningState, retries);
	}
}
