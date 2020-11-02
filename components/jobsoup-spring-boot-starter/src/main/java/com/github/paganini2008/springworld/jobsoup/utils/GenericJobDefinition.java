package com.github.paganini2008.springworld.jobsoup.utils;

import com.github.paganini2008.springworld.jobsoup.DependencyType;
import com.github.paganini2008.springworld.jobsoup.GenericTrigger;
import com.github.paganini2008.springworld.jobsoup.JobDefinition;
import com.github.paganini2008.springworld.jobsoup.JobKey;
import com.github.paganini2008.springworld.jobsoup.NotManagedJob;
import com.github.paganini2008.springworld.jobsoup.Trigger;
import com.github.paganini2008.springworld.jobsoup.model.JobPersistParam;
import com.github.paganini2008.springworld.jobsoup.model.JobTriggerParam;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * GenericJobDefinition
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class GenericJobDefinition implements JobDefinition {

	// Generial settings
	private final JobKey jobKey;
	private final String description;
	private final String email;
	private final int retries;
	private final int weight;
	private final long timeout;

	// Dependency settings
	private final JobKey[] dependentKeys;
	private final JobKey[] subKeys;
	private final float completionRate;

	// Trigger settings
	private final Trigger trigger;

	GenericJobDefinition(Builder builder) {
		this.jobKey = builder.jobKey;
		this.description = builder.description;
		this.email = builder.email;
		this.retries = builder.retries;
		this.weight = builder.weight;
		this.timeout = builder.timeout;
		this.trigger = builder.trigger;
		this.dependentKeys = builder.dependentKeys;
		this.subKeys = builder.subKeys;
		this.completionRate = builder.completionRate;
	}

	@Override
	public String getClusterName() {
		return jobKey.getClusterName();
	}

	@Override
	public String getGroupName() {
		return jobKey.getGroupName();
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
	public Trigger getTrigger() {
		return trigger;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getRetries() {
		return retries;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public JobKey[] getDependentKeys() {
		return dependentKeys;
	}

	@Override
	public JobKey[] getSubKeys() {
		return subKeys;
	}

	@Override
	public float getCompletionRate() {
		return completionRate;
	}

	@Accessors(chain = true)
	@Getter
	@Setter
	public static class Builder {

		private final JobKey jobKey;
		private String description = "";
		private String email = "";
		private int retries;
		private int weight = 100;
		private long timeout = -1L;

		private DependencyType dependencyType = DependencyType.SERIAL;
		private JobKey[] dependentKeys;
		private JobKey[] subKeys;
		private float completionRate = -1F;

		private Trigger trigger;

		Builder(JobKey jobKey) {
			this.jobKey = jobKey;
		}

		Builder(String clusterName, String groupName, String jobName, Class<? extends NotManagedJob> jobClass) {
			this.jobKey = JobKey.by(clusterName, groupName, jobName, jobClass.getName());
		}

		public GenericJobDefinition build() {
			return new GenericJobDefinition(this);
		}
	}

	public JobPersistParam toParameter() {
		JobPersistParam param = new JobPersistParam();
		param.setDescription(description);
		param.setEmail(email);
		param.setRetries(retries);
		param.setTimeout(timeout);
		param.setWeight(weight);
		param.setJobKey(jobKey);
		param.setDependentKeys(dependentKeys);
		param.setSubKeys(subKeys);
		param.setCompletionRate(completionRate);

		JobTriggerParam triggerParam = new JobTriggerParam();
		triggerParam.setTriggerType(trigger.getTriggerType());
		triggerParam.setTriggerDescription(trigger.getTriggerDescription());
		triggerParam.setStartDate(trigger.getStartDate());
		triggerParam.setEndDate(trigger.getEndDate());
		triggerParam.setRepeatCount(trigger.getRepeatCount());
		param.setTrigger(triggerParam);

		return param;
	}

	public static Builder parse(JobPersistParam param) {
		Builder builder = newJob(param.getJobKey()).setDescription(param.getDescription()).setEmail(param.getEmail())
				.setRetries(param.getRetries()).setTimeout(param.getTimeout()).setWeight(param.getWeight())
				.setDependentKeys(param.getDependentKeys()).setSubKeys(param.getSubKeys());
		GenericTrigger.Builder triggerBuilder = GenericTrigger.parse(param.getTrigger());
		builder.setTrigger(triggerBuilder.build());
		return builder;
	}

	public static Builder newJob(JobKey jobKey) {
		return new Builder(jobKey);
	}

	public static Builder newJob(String clusterName, String groupName, String jobName, Class<? extends NotManagedJob> jobClass) {
		return new Builder(clusterName, groupName, jobName, jobClass);
	}

}
