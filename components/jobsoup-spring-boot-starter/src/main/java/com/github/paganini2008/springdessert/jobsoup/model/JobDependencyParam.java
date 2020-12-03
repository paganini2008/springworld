package com.github.paganini2008.springdessert.jobsoup.model;

import com.github.paganini2008.springdessert.jobsoup.DependencyType;
import com.github.paganini2008.springdessert.jobsoup.JobKey;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobDependencyParam
 * 
 * @author Jimmy Hoff
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
