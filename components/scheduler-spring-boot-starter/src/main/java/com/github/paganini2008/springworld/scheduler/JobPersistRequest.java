package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * JobPersistRequest
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobPersistRequest implements Job {

	private final JobConfig jobConfig;

	JobPersistRequest(JobConfig jobConfig) {
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
	public TriggerBuilder buildTrigger() {
		return TriggerBuilder.newTrigger(jobConfig.getTriggerType()).setStartDate(jobConfig.getStartDate())
				.setEndDate(jobConfig.getEndDate()).setTriggerDescription(jobConfig.getTriggerDescription());
	}

	@Override
	public Object execute(JobKey jobKey, Object result) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean managedByApplicationContext() {
		return false;
	}

	public static JobPersistRequest build(JobConfig jobConfig) {
		return new JobPersistRequest(jobConfig);
	}

}