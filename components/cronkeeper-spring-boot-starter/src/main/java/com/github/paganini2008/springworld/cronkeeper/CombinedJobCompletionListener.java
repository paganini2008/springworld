package com.github.paganini2008.springworld.cronkeeper;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cronkeeper.model.JobTriggerDetail;
import com.github.paganini2008.springworld.reditools.common.RedisCountDownLatch;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CombinedJobCompletionListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class CombinedJobCompletionListener implements JobRuntimeListener {

	@Autowired
	private JobManager jobManager;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void afterRun(long traceId, JobKey jobKey, Object attachment, Date startDate, RunningState runningState, Object result,
			Throwable reason) {
		JobTriggerDetail triggerDetail;
		try {
			triggerDetail = jobManager.getJobTriggerDetail(jobKey);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return;
		}
		if (triggerDetail.getTriggerType() != TriggerType.COMBINED) {
			return;
		}
		RedisCountDownLatch latch = new RedisCountDownLatch(jobKey.getIdentifier(), redisMessageSender);
		JobPeerResult jobPeerResult = new JobPeerResult(jobKey, attachment);
		jobPeerResult.setResult(result);
		jobPeerResult.setApproved(runningState == RunningState.COMPLETED || runningState == RunningState.FINISHED);
		latch.countdown(jobPeerResult);
	}

}
