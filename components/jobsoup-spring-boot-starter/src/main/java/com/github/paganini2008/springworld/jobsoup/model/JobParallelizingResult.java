package com.github.paganini2008.springworld.jobsoup.model;

import com.github.paganini2008.springworld.jobsoup.JobKey;

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
	private JobPeerResult[] jobPeerResults;

	public JobParallelizingResult() {
	}

	public JobParallelizingResult(JobKey jobKey, Object attachment, JobPeerResult[] jobPeerResults) {
		this.jobKey = jobKey;
		this.attachment = attachment;
		this.jobPeerResults = jobPeerResults;
	}

}
