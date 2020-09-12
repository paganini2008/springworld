package com.github.paganini2008.springworld.crontab.server;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.springworld.crontab.Job;
import com.github.paganini2008.springworld.crontab.JobDependencyObservable;
import com.github.paganini2008.springworld.crontab.JobException;
import com.github.paganini2008.springworld.crontab.JobExecutor;
import com.github.paganini2008.springworld.crontab.JobKey;
import com.github.paganini2008.springworld.crontab.JobLoggerFactory;
import com.github.paganini2008.springworld.crontab.JobManager;
import com.github.paganini2008.springworld.crontab.JobTemplate;
import com.github.paganini2008.springworld.crontab.LogManager;
import com.github.paganini2008.springworld.crontab.RetryPolicy;
import com.github.paganini2008.springworld.crontab.RunningState;
import com.github.paganini2008.springworld.crontab.StopWatch;
import com.github.paganini2008.springworld.redisplus.common.RedisUUID;

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
	private RetryPolicy retryPolicy;

	@Autowired
	private RedisUUID redisUUID;

	@Autowired
	private LogManager logManager;

	@Override
	protected long getTraceId(JobKey jobKey) {
		return redisUUID.createUUID().timestamp();
	}

	@Override
	public void execute(Job job, Object attachment, int retries) {
		runJob(job, attachment, retries);
	}

	@Override
	protected void beforeRun(long traceId, JobKey jobKey, Job job, Date startDate) {
		setLogger(JobLoggerFactory.getLogger(log, traceId, jobKey, logManager));
		super.beforeRun(traceId, jobKey, job, startDate);
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
	protected Object retry(JobKey jobKey, Job job, Object attachment, Throwable reason, int retries) throws Throwable {
		return retryPolicy.retryIfNecessary(jobKey, job, attachment, reason, retries);
	}

	@Override
	protected void afterRun(long traceId, JobKey jobKey, Job job, Date startTime, RunningState runningState, Throwable reason,
			int retries) {
		super.afterRun(traceId, jobKey, job, startTime, runningState, reason, retries);
		stopWatch.finishJob(traceId, jobKey, startTime, runningState, reason != null ? ExceptionUtils.toArray(reason) : null, retries);
	}
}
