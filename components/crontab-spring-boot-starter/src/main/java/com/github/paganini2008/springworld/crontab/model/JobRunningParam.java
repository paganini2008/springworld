package com.github.paganini2008.springworld.crontab.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.paganini2008.springworld.crontab.JobKey;
import com.github.paganini2008.springworld.crontab.RunningState;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobRunningParam
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
@JsonInclude(value = Include.NON_NULL)
public class JobRunningParam {

	private JobKey jobKey;
	private Date startTime;
	private RunningState runningState;
	private String[] errorStackTracks;

	public JobRunningParam(JobKey jobKey, Date startTime, RunningState runningState, String[] errorStackTracks) {
		this.jobKey = jobKey;
		this.startTime = startTime;
		this.runningState = runningState;
		this.errorStackTracks = errorStackTracks;
	}

	public JobRunningParam(JobKey jobKey, Date startTime) {
		this.jobKey = jobKey;
		this.startTime = startTime;
	}
	
	public JobRunningParam() {
	}

}
