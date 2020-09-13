package com.github.paganini2008.springworld.cronfall.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.springworld.cronfall.Job;
import com.github.paganini2008.springworld.cronfall.JobAdmin;
import com.github.paganini2008.springworld.cronfall.JobKey;
import com.github.paganini2008.springworld.cronfall.JobManager;
import com.github.paganini2008.springworld.cronfall.JobState;
import com.github.paganini2008.springworld.cronfall.TriggerBuilder;
import com.github.paganini2008.springworld.cronfall.model.JobTriggerDetail;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ServerModeJobBeanProxy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ServerModeJobBeanProxy implements Job {

	private final JobKey jobKey;
	private final JobTriggerDetail triggerDetail;

	@Autowired
	private JobAdmin jobAdmin;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private ClusterRegistry clusterRegistry;

	public ServerModeJobBeanProxy(JobKey jobKey, JobTriggerDetail triggerDetail) {
		this.jobKey = jobKey;
		this.triggerDetail = triggerDetail;
	}

	@Override
	public String getClusterName() {
		return jobKey.getClusterName();
	}

	@Override
	public String getJobName() {
		return jobKey.getJobName();
	}

	@Override
	public String getJobClassName() {
		return jobKey.getJobClassName();
	}

	@Override
	public String getGroupName() {
		return jobKey.getGroupName();
	}

	@Override
	public TriggerBuilder buildTrigger() {
		return TriggerBuilder.newTrigger(triggerDetail.getTriggerType()).setStartDate(triggerDetail.getStartDate())
				.setEndDate(triggerDetail.getEndDate()).setTriggerDescription(triggerDetail.getTriggerDescriptionObject());
	}

	@Override
	public boolean managedByApplicationContext() {
		return false;
	}

	@Override
	public Object execute(JobKey jobKey, Object result) {
		try {
			return jobAdmin.triggerJob(jobKey, result);
		} catch (RestClientException e) {
			resetJobState();
			clusterRegistry.unregisterCluster(jobKey.getClusterName());
			log.error(e.getMessage(), e);
		} catch (NoJobResourceException e) {
			resetJobState();
			log.info("Job: " + jobKey.toString() + " is not available now.");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return JobState.NONE;
	}

	private void resetJobState() {
		try {
			jobManager.setJobState(jobKey, JobState.SCHEDULING);
		} catch (Exception ignored) {
		}
	}

	@Override
	public void onFailure(JobKey jobKey, Throwable e) {
		log.error(e.getMessage(), e);
	}

}
