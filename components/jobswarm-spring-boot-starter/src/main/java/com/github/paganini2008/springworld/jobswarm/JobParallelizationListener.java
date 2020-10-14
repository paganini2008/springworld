package com.github.paganini2008.springworld.jobswarm;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.jobswarm.model.JobPeerResult;
import com.github.paganini2008.springworld.jobswarm.model.JobRuntime;
import com.github.paganini2008.springworld.reditools.common.RedisCountDownLatch;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

/**
 * 
 * JobParallelizationListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobParallelizationListener implements JobRuntimeListener {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private JobManager jobManager;

	@Override
	public void afterRun(long traceId, JobKey jobKey, Object attachment, Date startDate, RunningState runningState, Object result,
			Throwable reason) {
		JobKey relation;
		JobRuntime jobRuntime;
		try {
			relation = jobManager.getRelations(jobKey, DependencyType.PARALLEL)[0];
			jobRuntime = jobManager.getJobRuntime(relation);
		} catch (Exception e) {
			throw ExceptionUtils.wrapExeception(e);
		}
		if (jobRuntime.getJobState() != JobState.RUNNING) {
			return;
		}

		RedisCountDownLatch latch = new RedisCountDownLatch(relation.getIdentifier(), redisMessageSender);
		JobPeerResult jobPeerResult = new JobPeerResult(jobKey, attachment);
		jobPeerResult.setResult(result);
		jobPeerResult.setApproved(runningState == RunningState.COMPLETED || runningState == RunningState.FINISHED);
		latch.countdown(jobPeerResult);
	}

}
