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

	private DependencyType dependencyType;
	private JobKey[] dependencies;
	private float completionRate;
	private String[] approvalClassNames;

	public JobDependencyParam() {
	}

	public JobDependencyParam(DependencyType dependencyType, JobKey[] dependencies, float completionRate, String[] approvalClassNames) {
		this.dependencyType = dependencyType;
		this.dependencies = dependencies;
		this.completionRate = completionRate;
		this.approvalClassNames = approvalClassNames;
	}

}
