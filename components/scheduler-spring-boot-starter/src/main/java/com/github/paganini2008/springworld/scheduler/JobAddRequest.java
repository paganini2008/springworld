package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobAddRequest
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobAddRequest implements Job {

	private final JobConfig jobConfig;

	JobAddRequest(JobConfig jobConfig) {
		this.jobConfig = jobConfig;
	}

	@Override
	public String getJobName() {
		return jobConfig.getJobName();
	}

	@Override
	public String getJobClassName() {
		return jobConfig.getJobClassName();
	}

	@Override
	public String getDescription() {
		return jobConfig.getDescription();
	}

	@Override
	public int getRetries() {
		return jobConfig.getRetries();
	}

	@Override
	public String getEmail() {
		return jobConfig.getEmail();
	}

	@Override
	public String getGroupName() {
		return jobConfig.getGroupName();
	}

	@Override
	public TriggerType getTriggerType() {
		return jobConfig.getTriggerType();
	}

	@Override
	public TriggerDescription getTriggerDescription() {
		return jobConfig.getTriggerDescription();
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
