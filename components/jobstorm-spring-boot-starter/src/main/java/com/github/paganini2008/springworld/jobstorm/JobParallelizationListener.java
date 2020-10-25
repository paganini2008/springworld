package com.github.paganini2008.springworld.jobstorm;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.jobstorm.model.JobPeerResult;
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
		try {
			relation = jobManager.getRelations(jobKey, DependencyType.PARALLEL)[0];
		} catch (Exception e) {
			throw ExceptionUtils.wrapExeception("Job '" + jobKey + "' has no relations", e);
		}

		RedisCountDownLatch latch = new RedisCountDownLatch(relation.getIdentifier(), redisMessageSender);
		latch.countdown(new JobPeerResult(jobKey, attachment, runningState, result));
	}

}
