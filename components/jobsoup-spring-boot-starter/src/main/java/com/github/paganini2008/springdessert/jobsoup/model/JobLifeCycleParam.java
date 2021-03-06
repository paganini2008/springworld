package com.github.paganini2008.springdessert.jobsoup.model;

import com.github.paganini2008.devtools.beans.ToStringBuilder;
import com.github.paganini2008.springdessert.jobsoup.JobKey;
import com.github.paganini2008.springdessert.jobsoup.JobLifeCycle;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobLifeCycleParam
 * 
 * @author Jimmy Hoff
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
