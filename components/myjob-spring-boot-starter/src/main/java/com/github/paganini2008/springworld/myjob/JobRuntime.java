package com.github.paganini2008.springworld.myjob;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;

/**
 * 
 * JobRuntime
 *
 * @author Fred Feng
 * @since 1.0
 */
@Getter
public class JobRuntime implements Serializable {

	private static final long serialVersionUID = -6283587791317006889L;
	private int jobId;
	private JobState jobState;
	private RunningState lastRunningState;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastExecutionTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastCompletionTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date nextExecutionTime;

	public JobRuntime() {
	}

	public void setJobState(int jobState) {
		this.jobState = JobState.valueOf(jobState);
	}

	public void setLastRunningState(int runningState) {
		this.lastRunningState = RunningState.valueOf(runningState);
	}

	public void setLastExecutionTime(Date lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}

	public void setLastCompletionTime(Date lastCompletionTime) {
		this.lastCompletionTime = lastCompletionTime;
	}

	public void setNextExecutionTime(Date nextExecutionTime) {
		this.nextExecutionTime = nextExecutionTime;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
