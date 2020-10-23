package com.github.paganini2008.springworld.jobstorm.model;

import com.github.paganini2008.springworld.jobstorm.DependencyType;
import com.github.paganini2008.springworld.jobstorm.JobKey;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobDependencyParam
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobDependencyParam {

	private JobKey jobKey;
	private DependencyType dependencyType;

	public JobDependencyParam() {
	}

	public JobDependencyParam(JobKey jobKey, DependencyType dependencyType) {
		this.jobKey = jobKey;
		this.dependencyType = dependencyType;
	}

}
