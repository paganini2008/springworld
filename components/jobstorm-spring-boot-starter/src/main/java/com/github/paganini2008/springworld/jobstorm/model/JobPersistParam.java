package com.github.paganini2008.springworld.jobstorm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.paganini2008.springworld.jobstorm.DependencyType;
import com.github.paganini2008.springworld.jobstorm.JobKey;

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
	private DependencyType dependencyType = DependencyType.SERIAL;
	private JobKey[] dependencies;
	private float completionRate = -1F;

	private JobTriggerParam trigger;
	private String attachment;

	public JobPersistParam() {
	}

	public JobPersistParam(String clusterName, String groupName, String jobName, String jobClassName) {
		this.jobKey = JobKey.by(clusterName, groupName, jobName, jobClassName);
	}

}
