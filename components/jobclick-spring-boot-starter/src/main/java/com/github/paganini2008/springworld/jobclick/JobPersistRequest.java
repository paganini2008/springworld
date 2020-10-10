package com.github.paganini2008.springworld.jobclick;

import com.github.paganini2008.springworld.jobclick.model.JobPersistParam;

/**
 * 
 * JobPersistRequest
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobPersistRequest implements JobDefinition {

	private final JobPersistParam persistParam;

	JobPersistRequest(JobPersistParam persistParam) {
		this.persistParam = persistParam;
	}

	@Override
	public String getClusterName() {
		return persistParam.getClusterName();
	}

	@Override
	public String getGroupName() {
		return persistParam.getGroupName();
	}

	@Override
	public String getJobName() {
		return persistParam.getJobName();
	}

	@Override
	public String getJobClassName() {
		return persistParam.getJobClassName();
	}

	@Override
	public String getDescription() {
		return persistParam.getDescription();
	}

	@Override
	public int getRetries() {
		return persistParam.getRetries();
	}

	@Override
	public String getEmail() {
		return persistParam.getEmail();
	}

	@Override
	public TriggerBuilder buildTrigger() {
		return TriggerBuilder.newTrigger(persistParam.getTriggerType()).setStartDate(persistParam.getStartDate())
				.setEndDate(persistParam.getEndDate()).setTriggerDescription(persistParam.getTriggerDescription());
	}

	public static JobPersistRequest build(JobPersistParam persistParam) {
		return new JobPersistRequest(persistParam);
	}

}
