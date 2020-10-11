package com.github.paganini2008.springworld.jobclick;

import org.slf4j.Logger;

import com.github.paganini2008.springworld.jobclick.model.JobTriggerDetail;

/**
 * 
 * ExternalJobBeanProxy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ExternalJobBeanProxy implements Job {

	private final JobKey jobKey;
	private final JobTriggerDetail triggerDetail;

	public ExternalJobBeanProxy(JobKey jobKey, JobTriggerDetail triggerDetail) {
		this.jobKey = jobKey;
		this.triggerDetail = triggerDetail;
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
	public String getClusterName() {
		return jobKey.getClusterName();
	}

	@Override
	public Trigger getTrigger() {
		return new BasicTrigger(triggerDetail.getTriggerType()).setStartDate(triggerDetail.getStartDate())
				.setEndDate(triggerDetail.getEndDate()).setTriggerDescription(triggerDetail.getTriggerDescriptionObject());
	}

	@Override
	public Object execute(JobKey jobKey, Object result, Logger log) {
		throw new UnsupportedOperationException("execute");
	}

	@Override
	public boolean managedByApplicationContext() {
		return false;
	}

}
