package com.github.paganini2008.springworld.joblink;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.joblink.model.JobPeerResult;
import com.github.paganini2008.springworld.reditools.common.RedisCountDownLatch;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

/**
 * 
 * JobSetCompletionListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobSetCompletionListener implements JobRuntimeListener {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void afterRun(long traceId, JobKey jobKey, Object attachment, Date startDate, RunningState runningState, Object result,
			Throwable reason) {
		RedisCountDownLatch latch = new RedisCountDownLatch(jobKey.getIdentifier(), redisMessageSender);
		JobPeerResult jobPeerResult = new JobPeerResult(jobKey, attachment);
		jobPeerResult.setResult(result);
		jobPeerResult.setApproved(runningState == RunningState.COMPLETED || runningState == RunningState.FINISHED);
		latch.countdown(jobPeerResult);
	}

}
