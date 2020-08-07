package com.github.paganini2008.springworld.scheduler;

import java.io.Serializable;

import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;

/**
 * 
 * JobResult
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
public class JobResult implements Serializable {

	private static final long serialVersionUID = -6257798137365527003L;

	private boolean success;
	private String msg;
	private JobState jobState;

	public static JobResult success(JobState jobState, String msg) {
		JobResult jobResult = new JobResult();
		jobResult.setSuccess(true);
		jobResult.setMsg(msg);
		jobResult.setJobState(jobState);
		return jobResult;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setJobState(int jobState) {
		this.jobState = JobState.valueOf(jobState);
	}

	public void setJobState(JobState jobState) {
		this.jobState = jobState;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
