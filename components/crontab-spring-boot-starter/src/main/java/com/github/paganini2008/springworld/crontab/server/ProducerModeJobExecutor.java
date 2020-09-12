package com.github.paganini2008.springworld.crontab.server;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.crontab.Job;
import com.github.paganini2008.springworld.crontab.JobException;
import com.github.paganini2008.springworld.crontab.JobExecutor;
import com.github.paganini2008.springworld.crontab.JobKey;
import com.github.paganini2008.springworld.crontab.JobManager;
import com.github.paganini2008.springworld.crontab.JobState;
import com.github.paganini2008.springworld.crontab.JobTemplate;
import com.github.paganini2008.springworld.crontab.RunningState;
import com.github.paganini2008.springworld.crontab.ScheduleManager;
import com.github.paganini2008.springworld.crontab.StopWatch;
import com.github.paganini2008.springworld.redisplus.common.RedisUUID;

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
	private RedisUUID redisUUID;

	@Override
	protected long getTraceId(JobKey jobKey) {
		return redisUUID.createUUID().timestamp();
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
