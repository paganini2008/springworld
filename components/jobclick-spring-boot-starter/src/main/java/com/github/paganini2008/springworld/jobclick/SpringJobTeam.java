package com.github.paganini2008.springworld.jobclick;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springworld.jobclick.model.JobPeer;
import com.github.paganini2008.springworld.jobclick.model.JobPeerResult;
import com.github.paganini2008.springworld.jobclick.model.JobTeamResult;
import com.github.paganini2008.springworld.jobclick.model.JobTriggerDetail;
import com.github.paganini2008.springworld.reditools.common.RedisCountDownLatch;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SpringJobTeam
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class SpringJobTeam implements Runnable, JobTeam {

	@Qualifier(BeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Qualifier(BeanNames.INTERNAL_JOB_BEAN_LOADER)
	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Qualifier(BeanNames.EXTERNAL_JOB_BEAN_LOADER)
	@Autowired(required = false)
	private JobBeanLoader externalJobBeanLoader;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private JobManager jobManager;

	private final Job job;
	private final JobPeer[] jobPeers;

	public SpringJobTeam(Job job, JobPeer[] jobPeers) {
		this.job = job;
		this.jobPeers = jobPeers;
	}

	private Object attachment;

	@Override
	public void cooperate(Object attachment) {
		this.attachment = attachment;
		run();
	}

	public Object getAttachment() {
		return attachment;
	}

	@Override
	public void run() {
		for (JobPeer jobPeer : jobPeers) {
			try {
				Job peerJob = getJob(jobPeer.getJobKey());
				jobExecutor.execute(peerJob, jobPeer.getAttachment(), 0);
				log.trace("Run job peer: " + jobPeer.getJobKey());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		final JobKey jobKey = JobKey.of(job);
		log.trace("Job '{}' is waiting for all job peers done ...", jobKey);
		RedisCountDownLatch latch = new RedisCountDownLatch(jobKey.getIdentifier(), redisMessageSender);
		Object[] answer = job.getTimeout() > 0
				? latch.await(jobPeers.length, job.getTimeout(), TimeUnit.MILLISECONDS, null)
				: latch.await(jobPeers.length, null);
		if (ArrayUtils.isNotEmpty(answer)) {
			Map<JobKey, JobPeerResult> mapper = mapResult(answer);
			JobTriggerDetail triggerDetail;
			try {
				triggerDetail = jobManager.getJobTriggerDetail(jobKey);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return;
			}
			boolean run = true;
			Float goal = triggerDetail.getTriggerDescriptionObject().getMilestone().getGoal();
			if (goal != null) {
				float total = 0;
				for (JobPeer jobPeer : jobPeers) {
					if (mapper.get(jobPeer.getJobKey()).isApproved()) {
						total += jobPeer.getBoost();
					}
				}
				run = total >= goal.floatValue();
			}
			if (run) {
				log.trace("Do run job '{}' after all job peers done.", jobKey);
				JobTeamResult teamResult = new JobTeamResult(jobKey, attachment, mapper.values().toArray(new JobPeerResult[0]));
				jobExecutor.execute(job, teamResult, 0);
			}
		}
	}

	private Map<JobKey, JobPeerResult> mapResult(Object[] answer) {
		Map<JobKey, JobPeerResult> data = new HashMap<JobKey, JobPeerResult>();
		for (Object result : answer) {
			JobPeerResult peerResult = (JobPeerResult) result;
			data.put(peerResult.getJobKey(), peerResult);
		}
		return data;
	}

	private Job getJob(JobKey jobKey) throws Exception {
		Job job = jobBeanLoader.loadJobBean(jobKey);
		if (job == null && externalJobBeanLoader != null) {
			job = externalJobBeanLoader.loadJobBean(jobKey);
		}
		return job;
	}

}
