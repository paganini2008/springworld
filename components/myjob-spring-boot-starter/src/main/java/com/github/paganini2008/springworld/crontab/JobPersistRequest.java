package com.github.paganini2008.springworld.crontab;

/**
 * 
 * JobPersistRequest
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobPersistRequest implements JobDefinition {

	private final JobConfig jobConfig;

	JobPersistRequest(JobConfig jobConfig) {
		this.jobConfig = jobConfig;
	}

	@Override
	public String getClusterName() {
		return jobConfig.getClusterName();
	}

	@Override
	public String getGroupName() {
		return jobConfig.getGroupName();
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
	public TriggerBuilder buildTrigger() {
		return TriggerBuilder.newTrigger(jobConfig.getTriggerType()).setStartDate(jobConfig.getStartDate())
				.setEndDate(jobConfig.getEndDate()).setTriggerDescription(jobConfig.getTriggerDescription());
	}

	public static JobPersistRequest build(JobConfig jobConfig) {
		return new JobPersistRequest(jobConfig);
	}

}
