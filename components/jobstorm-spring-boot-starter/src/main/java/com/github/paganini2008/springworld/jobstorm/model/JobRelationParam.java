package com.github.paganini2008.springworld.jobstorm.model;

import com.github.paganini2008.springworld.jobstorm.DependencyType;
import com.github.paganini2008.springworld.jobstorm.JobKey;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobRelationParam
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobRelationParam {

	private JobKey jobKey;
	private DependencyType dependencyType;

	public JobRelationParam() {
	}

	public JobRelationParam(JobKey jobKey, DependencyType dependencyType) {
		this.jobKey = jobKey;
		this.dependencyType = dependencyType;
	}

}
