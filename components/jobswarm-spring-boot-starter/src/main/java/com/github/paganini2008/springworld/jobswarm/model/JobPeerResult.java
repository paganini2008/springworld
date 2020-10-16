package com.github.paganini2008.springworld.jobswarm.model;

import com.github.paganini2008.springworld.jobswarm.JobKey;
import com.github.paganini2008.springworld.jobswarm.RunningState;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobPeerResult
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobPeerResult {

	private JobKey jobKey;
	private Object attachment;
	private RunningState runningState;
	private Object result;

	public JobPeerResult() {
	}

	public JobPeerResult(JobKey jobKey, Object attachment, RunningState runningState, Object result) {
		this.jobKey = jobKey;
		this.attachment = attachment;
		this.runningState = runningState;
		this.result = result;
	}

}
