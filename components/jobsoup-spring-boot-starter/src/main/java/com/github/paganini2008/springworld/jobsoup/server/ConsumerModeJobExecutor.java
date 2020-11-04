package com.github.paganini2008.springworld.jobsoup.server;

import java.util.Date;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.jobsoup.DependencyType;
import com.github.paganini2008.springworld.jobsoup.Job;
import com.github.paganini2008.springworld.jobsoup.JobException;
import com.github.paganini2008.springworld.jobsoup.JobExecutor;
import com.github.paganini2008.springworld.jobsoup.JobKey;
import com.github.paganini2008.springworld.jobsoup.JobLoggerFactory;
import com.github.paganini2008.springworld.jobsoup.JobManager;
import com.github.paganini2008.springworld.jobsoup.JobRuntimeListenerContainer;
import com.github.paganini2008.springworld.jobsoup.JobTemplate;
import com.github.paganini2008.springworld.jobsoup.LogManager;
import com.github.paganini2008.springworld.jobsoup.RetryPolicy;
import com.github.paganini2008.springworld.jobsoup.RunningState;
import com.github.paganini2008.springworld.jobsoup.SerialDependencyScheduler;
import com.github.paganini2008.springworld.jobsoup.StopWatch;
import com.github.paganini2008.springworld.jobsoup.TraceIdGenerator;
import com.github.paganini2008.springworld.jobsoup.utils.JavaMailService;

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
	private SerialDependencyScheduler serialDependencyScheduler;

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

	@Autowired(required = false)
	private JavaMailService mailService;

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
				serialDependencyScheduler.notifyDependants(jobKey, result);
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
		stopWatch.onJobEnd(traceId, jobKey, startDate, runningState, retries);
		jobRuntimeListenerContainer.afterRun(traceId, jobKey, job, attachment, startDate, runningState, result, reason, retries);
	}

	@Override
	protected void sendMail(String mailAddress, long traceId, JobKey jobKey, Object attachment, Date startDate, RunningState runningState,
			Throwable reason) {
		if (mailService != null) {
			mailService.sendMail(mailAddress, traceId, jobKey, attachment, startDate, runningState, reason);
		}
	}
}