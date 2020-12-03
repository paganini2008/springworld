package com.github.paganini2008.springdessert.jobsoup.model;

import com.github.paganini2008.springdessert.jobsoup.JobKey;
import com.github.paganini2008.springdessert.jobsoup.JobState;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobStateParam
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobStateParam {

	private JobKey jobKey;
	private JobState jobState;

	public JobStateParam() {
	}

	public JobStateParam(JobKey jobKey, JobState jobState) {
		this.jobKey = jobKey;
		this.jobState = jobState;
	}

}
