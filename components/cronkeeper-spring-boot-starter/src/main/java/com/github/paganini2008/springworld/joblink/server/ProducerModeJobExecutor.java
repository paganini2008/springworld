package com.github.paganini2008.springworld.joblink.server;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.joblink.Job;
import com.github.paganini2008.springworld.joblink.JobException;
import com.github.paganini2008.springworld.joblink.JobExecutor;
import com.github.paganini2008.springworld.joblink.JobKey;
import com.github.paganini2008.springworld.joblink.JobManager;
import com.github.paganini2008.springworld.joblink.JobState;
import com.github.paganini2008.springworld.joblink.JobTemplate;
import com.github.paganini2008.springworld.joblink.RunningState;
import com.github.paganini2008.springworld.joblink.ScheduleManager;
import com.github.paganini2008.springworld.joblink.StopWatch;
import com.github.paganini2008.springworld.joblink.TraceIdGenerator;

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

	@Override
	protected long getTraceId(JobKey jobKey) {
		return idGenerator.generateTraceId(jobKey);
	}

	@Override
	public void execute(Job job, Object attachment, int retries) {
		runJob(job, attachment, retries);
	}

	@Override
	protected void beforeRun(long traceId, JobKey jobKey, Job job, Object attachment, Date startTime) {
		super.beforeRun(traceId, jobKey, job, attachment, startTime);
		stopWatch.startJob(traceId, jobKey, startTime);
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
