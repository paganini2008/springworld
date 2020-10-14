package com.github.paganini2008.springworld.jobswarm.model;

import com.github.paganini2008.springworld.jobswarm.JobKey;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobParallelizingResult
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobParallelizingResult {

	private JobKey jobKey;
	private Object attachment;
	private JobPeerResult[] results;

	public JobParallelizingResult() {
	}

	public JobParallelizingResult(JobKey jobKey, Object attachment, JobPeerResult[] results) {
		this.jobKey = jobKey;
		this.attachment = attachment;
		this.results = results;
	}

}
