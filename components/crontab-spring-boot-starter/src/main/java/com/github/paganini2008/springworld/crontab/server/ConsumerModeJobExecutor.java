package com.github.paganini2008.springworld.crontab.server;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.springworld.crontab.Job;
import com.github.paganini2008.springworld.crontab.JobDependencyObservable;
import com.github.paganini2008.springworld.crontab.JobException;
import com.github.paganini2008.springworld.crontab.JobExecutor;
import com.github.paganini2008.springworld.crontab.JobKey;
import com.github.paganini2008.springworld.crontab.JobManager;
import com.github.paganini2008.springworld.crontab.JobTemplate;
import com.github.paganini2008.springworld.crontab.RetryPolicy;
import com.github.paganini2008.springworld.crontab.RunningState;
import com.github.paganini2008.springworld.crontab.StopWatch;

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
	protected Object retry(JobKey jobKey, Job job, Object attachment, Throwable reason, int retries) throws Throwable {
		return retryPolicy.retryIfNecessary(jobKey, job, attachment, reason, retries);
	}

	@Override
	protected void afterRun(JobKey jobKey, Job job, Date startTime, RunningState runningState, Throwable reason, int retries) {
		super.afterRun(jobKey, job, startTime, runningState, reason, retries);
		stopWatch.finishJob(jobKey, startTime, runningState, reason != null ? ExceptionUtils.toArray(reason) : null, retries);
	}
}
