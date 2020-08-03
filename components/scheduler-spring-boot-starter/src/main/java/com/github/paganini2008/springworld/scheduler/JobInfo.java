package com.github.paganini2008.springworld.scheduler;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

/**
 * 
 * JobInfo
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
public class JobInfo implements Serializable {

	private static final long serialVersionUID = -1528742044603986153L;

	private int jobId;
	private String jobName;
	private String jobClassName;
	private String groupName;
	private TriggerType jobType;
	private String description;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createDate;
	private JobState jobState;
	private RunningState lastRunningState;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastExecutionTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastCompletionTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date nextExecutionTime;

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public void setJobClassName(String jobClassName) {
		this.jobClassName = jobClassName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public void setJobType(int jobType) {
		this.jobType = TriggerType.valueOf(jobType);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCreateDate(long createDate) {
		this.createDate = new Date(createDate);
	}

	public void setJobState(int state) {
		this.jobState = JobState.valueOf(state);
	}

	public void setLastRunningState(int state) {
		this.lastRunningState = RunningState.valueOf(state);
	}

	public void setLastExecutionTime(long lastExecutionTime) {
		this.lastExecutionTime = new Date(lastExecutionTime);
	}

	public void setNextExecutionTime(long nextExecutionTime) {
		this.nextExecutionTime = new Date(nextExecutionTime);
	}

	public void setLastCompletionTime(long lastCompletionTime) {
		this.lastCompletionTime = new Date(lastCompletionTime);
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

}
