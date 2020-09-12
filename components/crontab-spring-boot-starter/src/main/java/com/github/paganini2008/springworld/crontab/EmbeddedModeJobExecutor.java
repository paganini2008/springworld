package com.github.paganini2008.springworld.crontab;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.redisplus.common.RedisUUID;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * EmbeddedModeJobExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class EmbeddedModeJobExecutor extends JobTemplate implements JobExecutor {

	@Autowired
	private JobManager jobManager;

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired
	private StopWatch stopWatch;

	@Autowired
	private JobDependencyObservable jobDependencyObservable;

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
	protected void beforeRun(long traceId, JobKey jobKey, Job job, Date startTime) {
		setLogger(JobLoggerFactory.getLogger(log, traceId, jobKey, logManager));
		super.beforeRun(traceId, jobKey, job, startTime);
		stopWatch.startJob(traceId, jobKey, startTime);
	}

	@Override
	public void execute(Job job, Object attachment, int retries) {
		runJob(job, attachment, retries);
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
