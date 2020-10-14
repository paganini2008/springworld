package com.github.paganini2008.springworld.jobswarm.model;

import com.github.paganini2008.devtools.beans.ToStringBuilder;
import com.github.paganini2008.springworld.jobswarm.JobKey;
import com.github.paganini2008.springworld.jobswarm.JobLifeCycle;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobLifeCycleParam
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobLifeCycleParam {

	private JobKey jobKey;
	private JobLifeCycle lifeCycle;

	public JobLifeCycleParam() {
	}

	public JobLifeCycleParam(JobKey jobKey, JobLifeCycle lifeCycle) {
		this.jobKey = jobKey;
		this.lifeCycle = lifeCycle;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
