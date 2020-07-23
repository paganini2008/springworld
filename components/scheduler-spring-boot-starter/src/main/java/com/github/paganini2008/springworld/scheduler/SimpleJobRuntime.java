package com.github.paganini2008.springworld.scheduler;

import java.util.Date;

import com.github.paganini2008.devtools.beans.ToStringBuilder;

/**
 * 
 * SimpleJobRuntime
 *
 * @author Fred Feng
 * @since 1.0
 */
public class SimpleJobRuntime implements JobRuntime {

	private String jobName;
	private JobState jobState;
	private RunningState runningState;
	private Date lastExecutionTime;
	private Date lastCompletionTime;
	private Date nextExecutionTime;
	private long completedCount;
	private long failedCount;
	private long skippedCount;

	public SimpleJobRuntime() {
	}

	public void setJobState(int jobState) {
		this.jobState = JobState.valueOf(jobState);
	}

	public void setRunningState(int runningState) {
		this.runningState = RunningState.valueOf(runningState);
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
	
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Override
	public String getJobName() {
		return jobName;
	}

	@Override
	public RunningState getRunningState() {
		return runningState;
	}

	@Override
	public JobState getJobState() {
		return jobState;
	}

	@Override
	public Date getLastExecutionTime() {
		return lastExecutionTime;
	}

	@Override
	public Date getLastCompletionTime() {
		return lastCompletionTime;
	}

	@Override
	public Date getNextExecutionTime() {
		return nextExecutionTime;
	}

	@Override
	public long getCompletedCount() {
		return completedCount;
	}

	@Override
	public long getFailedCount() {
		return failedCount;
	}

	@Override
	public long getSkippedCount() {
		return skippedCount;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
