package com.github.paganini2008.springworld.jobstorm.server;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.jobstorm.Job;
import com.github.paganini2008.springworld.jobstorm.JobException;
import com.github.paganini2008.springworld.jobstorm.JobExecutor;
import com.github.paganini2008.springworld.jobstorm.JobKey;
import com.github.paganini2008.springworld.jobstorm.JobManager;
import com.github.paganini2008.springworld.jobstorm.JobRuntimeListenerContainer;
import com.github.paganini2008.springworld.jobstorm.JobState;
import com.github.paganini2008.springworld.jobstorm.JobTemplate;
import com.github.paganini2008.springworld.jobstorm.RunningState;
import com.github.paganini2008.springworld.jobstorm.ScheduleManager;
import com.github.paganini2008.springworld.jobstorm.StopWatch;
import com.github.paganini2008.springworld.jobstorm.TraceIdGenerator;

/**
 * 
 * ProducerModeJobExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ProducerModeJobExecutor extends JobTemplate implements JobExecutor {

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private StopWatch stopWatch;

	@Autowired
	private TraceIdGenerator idGenerator;

	@Autowired
	private JobRuntimeListenerContainer jobRuntimeListenerContainer;

	@Override
	protected long getTraceId(JobKey jobKey) {
		return idGenerator.generateTraceId(jobKey);
	}

	@Override
	public void execute(Job job, Object attachment, int retries) {
		runJob(job, attachment, retries);
	}

	@Override
	protected void beforeRun(long traceId, JobKey jobKey, Job job, Object attachment, Date startDate) {
		super.beforeRun(traceId, jobKey, job, attachment, startDate);
		jobRuntimeListenerContainer.beforeRun(traceId, jobKey, job, attachment, startDate);
		stopWatch.onJobBegin(traceId, jobKey, startDate);
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
			jobManager.finishJob(jobKey);
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
