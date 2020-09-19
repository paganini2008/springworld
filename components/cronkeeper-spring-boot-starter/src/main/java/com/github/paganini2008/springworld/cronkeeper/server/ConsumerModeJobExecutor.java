package com.github.paganini2008.springworld.cronkeeper.server;

import java.util.Date;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cronkeeper.Job;
import com.github.paganini2008.springworld.cronkeeper.JobDependencyObservable;
import com.github.paganini2008.springworld.cronkeeper.JobException;
import com.github.paganini2008.springworld.cronkeeper.JobExecutor;
import com.github.paganini2008.springworld.cronkeeper.JobKey;
import com.github.paganini2008.springworld.cronkeeper.JobLoggerFactory;
import com.github.paganini2008.springworld.cronkeeper.JobManager;
import com.github.paganini2008.springworld.cronkeeper.JobTemplate;
import com.github.paganini2008.springworld.cronkeeper.LogManager;
import com.github.paganini2008.springworld.cronkeeper.RetryPolicy;
import com.github.paganini2008.springworld.cronkeeper.RunningState;
import com.github.paganini2008.springworld.cronkeeper.StopWatch;
import com.github.paganini2008.springworld.cronkeeper.TraceIdGenerator;

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
	protected void afterRun(long traceId, JobKey jobKey, Job job, Date startTime, RunningState runningState, Throwable reason,
			int retries) {
		super.afterRun(traceId, jobKey, job, startTime, runningState, reason, retries);
		stopWatch.finishJob(traceId, jobKey, startTime, runningState, retries);
	}
}