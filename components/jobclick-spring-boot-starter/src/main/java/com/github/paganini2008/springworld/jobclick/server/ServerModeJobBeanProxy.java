package com.github.paganini2008.springworld.jobclick.server;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.springworld.jobclick.BasicTrigger;
import com.github.paganini2008.springworld.jobclick.Job;
import com.github.paganini2008.springworld.jobclick.JobAdmin;
import com.github.paganini2008.springworld.jobclick.JobKey;
import com.github.paganini2008.springworld.jobclick.JobManager;
import com.github.paganini2008.springworld.jobclick.JobState;
import com.github.paganini2008.springworld.jobclick.Trigger;
import com.github.paganini2008.springworld.jobclick.model.JobTriggerDetail;

/**
 * 
 * ServerModeJobBeanProxy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
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
	public Trigger getTrigger() {
		return new BasicTrigger(triggerDetail.getTriggerType()).setStartDate(triggerDetail.getStartDate())
				.setEndDate(triggerDetail.getEndDate()).setTriggerDescription(triggerDetail.getTriggerDescriptionObject());
	}

	@Override
	public boolean managedByApplicationContext() {
		return false;
	}

	@Override
	public Object execute(JobKey jobKey, Object result, Logger log) {
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
	public void onFailure(JobKey jobKey, Throwable e, Logger log) {
		log.error(e.getMessage(), e);
	}

}
