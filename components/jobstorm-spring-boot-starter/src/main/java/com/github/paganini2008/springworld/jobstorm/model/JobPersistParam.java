package com.github.paganini2008.springworld.jobstorm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.paganini2008.springworld.jobstorm.JobKey;
import com.github.paganini2008.springworld.jobstorm.model.TriggerDescription.Dependency;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobPersistParam
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class JobPersistParam {

	private JobKey jobKey;
	private String description;
	private String email;
	private int retries;
	private long timeout = -1L;
	private int weight = 100;
	private JobKey[] dependentKeys;
	private JobKey[] subKeys;
	private float completionRate = -1F;

	private JobTriggerParam trigger;
	private String attachment;

	public JobPersistParam() {
	}

	public JobPersistParam(String clusterName, String groupName, String jobName, String jobClassName) {
		this.jobKey = JobKey.by(clusterName, groupName, jobName, jobClassName);
	}

	public JobPersistParam(JobKey jobKey) {
		this.jobKey = jobKey;
	}

	public static JobPersistParam forExample() {
		JobPersistParam param = new JobPersistParam();
		param.setJobKey(JobKey.by("yourCluster", "yourGroup", "yourJob", "com.yourcompany.yourapp.YourJob"));
		param.setDescription("Describe your job shortly");
		param.setEmail("Set your email if job run abnormally");
		param.setRetries(0);
		param.setTimeout(-1L);
		param.setAttachment("Set initial parameter of your job. Data with json format is recommended.");
		param.setTrigger(new JobTriggerParam("*/5 * * * * ?"));
		return param;
	}

	public static JobPersistParam wrap(JobDetail jobDetail) {
		JobPersistParam param = new JobPersistParam(jobDetail.getJobKey());
		param.setAttachment(jobDetail.getAttachment());
		param.setDescription(jobDetail.getDescription());
		param.setEmail(jobDetail.getEmail());
		param.setRetries(jobDetail.getRetries());
		param.setTimeout(jobDetail.getTimeout());
		param.setWeight(jobDetail.getWeight());

		JobTriggerDetail triggerDetail = jobDetail.getJobTriggerDetail();
		TriggerDescription triggerDescription = triggerDetail.getTriggerDescriptionObject();
		Dependency dependency = triggerDescription.getDependency();
		if (dependency != null) {
			param.setCompletionRate(dependency.getCompletionRate());
			param.setDependentKeys(dependency.getDependentKeys());
			param.setSubKeys(dependency.getSubKeys());
			triggerDescription.setCron(dependency.getCron());
			triggerDescription.setPeriodic(dependency.getPeriodic());
			triggerDescription.setDependency(null);
		}

		JobTriggerParam trigger = new JobTriggerParam();
		trigger.setTriggerType(triggerDetail.getTriggerType());
		trigger.setStartDate(triggerDetail.getStartDate());
		trigger.setEndDate(triggerDetail.getEndDate());
		trigger.setRepeatCount(triggerDetail.getRepeatCount());
		trigger.setTriggerDescription(triggerDescription);
		param.setTrigger(trigger);

		return param;
	}

}
