package com.github.paganini2008.springworld.jobclick;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.jobclick.model.JobPeerResult;
import com.github.paganini2008.springworld.jobclick.model.JobRuntime;
import com.github.paganini2008.springworld.jobclick.model.JobTriggerDetail;
import com.github.paganini2008.springworld.reditools.common.RedisCountDownLatch;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

/**
 * 
 * JobTeamCompletionListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobTeamCompletionListener implements JobRuntimeListener {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private JobManager jobManager;

	@Override
	public void afterRun(long traceId, JobKey jobKey, Object attachment, Date startDate, RunningState runningState, Object result,
			Throwable reason) {
		JobTriggerDetail triggerDetail;
		try {
			triggerDetail = jobManager.getJobTriggerDetail(jobKey);
		} catch (Exception e) {
			throw ExceptionUtils.wrapExeception(e);
		}
		if (triggerDetail.getTriggerType() != TriggerType.NONE) {
			return;
		}
		JobKey relation;
		JobRuntime jobRuntime;
		try {
			relation = jobManager.getRelations(jobKey)[0];
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
