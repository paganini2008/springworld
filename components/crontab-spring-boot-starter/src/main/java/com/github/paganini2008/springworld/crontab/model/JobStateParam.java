package com.github.paganini2008.springworld.crontab.model;

import com.github.paganini2008.springworld.crontab.JobKey;
import com.github.paganini2008.springworld.crontab.JobState;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobStateParam
 * 
 * @author Fred Feng
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
