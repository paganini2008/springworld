package com.github.paganini2008.springworld.joblink.model;

import com.github.paganini2008.springworld.joblink.JobKey;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobTeamResult
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobTeamResult {

	private JobKey jobKey;
	private Object attachment;
	private JobPeerResult[] results;

	public JobTeamResult() {
	}

	public JobTeamResult(JobKey jobKey, Object attachment, JobPeerResult[] results) {
		this.jobKey = jobKey;
		this.attachment = attachment;
		this.results = results;
	}

}
