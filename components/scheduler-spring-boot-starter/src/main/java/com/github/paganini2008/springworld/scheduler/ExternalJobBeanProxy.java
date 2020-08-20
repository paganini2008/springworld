package com.github.paganini2008.springworld.scheduler;

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
	public TriggerType getTriggerType() {
		return triggerDetail.getTriggerType();
	}

	@Override
	public TriggerDescription getTriggerDescription() {
		return triggerDetail.getTriggerDescription();
	}

	@Override
	public Object execute(JobKey jobKey, Object result) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean managedByApplicationContext() {
		return false;
	}

}
