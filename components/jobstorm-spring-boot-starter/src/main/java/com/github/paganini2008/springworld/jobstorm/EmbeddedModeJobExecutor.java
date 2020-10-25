package com.github.paganini2008.springworld.jobstorm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.jobstorm.utils.JavaMailService;

/**
 * 
 * EmbeddedModeJobExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class EmbeddedModeJobExecutor extends JobTemplate implements JobExecutor {

	@Autowired
	private JobManager jobManager;

	@Autowired
	private LogManager logManager;

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired
	private StopWatch stopWatch;

	@Autowired
	private SerialDependencyScheduler serialDependencyScheduler;

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
	protected void beforeRun(long traceId, JobKey jobKey, Job job, Object attachment, Date startDate) {
		super.beforeRun(traceId, jobKey, job, attachment, startDate);
		jobRuntimeListenerContainer.beforeRun(traceId, jobKey, job, attachment, startDate);
		stopWatch.startJob(traceId, jobKey, startDate);
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
			throw ExceptionUtils.wrapExeception(e);
		}
	}

	@Override
	protected void notifyDependants(JobKey jobKey, Job job, Object result) {
		try {
			if (jobManager.hasRelations(jobKey, DependencyType.SERIAL)) {
				serialDependencyScheduler.notifyDependants(jobKey, result);
			}
		} catch (Exception e) {
			throw ExceptionUtils.wrapExeception(e);
		}
	}

	@Override
	protected void cancel(JobKey jobKey, Job job, RunningState runningState, String msg, Throwable reason) {
		try {
			scheduleManager.unscheduleJob(jobKey);
			jobManager.deleteJob(jobKey);
		} catch (Exception e) {
			throw ExceptionUtils.wrapExeception(e);
		}

		if (StringUtils.isNotBlank(msg)) {
			log.info(msg);
		}
		if (reason != null) {
			log.error(reason.getMessage(), reason);
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
		stopWatch.finishJob(traceId, jobKey, startDate, runningState, retries);
		jobRuntimeListenerContainer.afterRun(traceId, jobKey, job, attachment, startDate, runningState, result, reason, retries);

		if ((runningState == RunningState.FAILED || runningState == RunningState.FINISHED) && StringUtils.isNotBlank(job.getEmail())) {

			if (mailService != null) {
				Map<String, Object> model = new HashMap<String, Object>();
				model.put("jobKey", jobKey);
				model.put("traceId", traceId);
				model.put("attachment", attachment);
				model.put("startDate", startDate);
				model.put("runningState", runningState);
				if (reason != null) {
					model.put("stackTraceArray", com.github.paganini2008.devtools.ExceptionUtils.toArray(reason));
				}
				mailService.sendHtmlMail(job.getEmail(), model);
			}
		}
	}

}
