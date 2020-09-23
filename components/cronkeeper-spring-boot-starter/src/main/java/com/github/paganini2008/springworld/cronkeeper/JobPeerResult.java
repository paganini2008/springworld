package com.github.paganini2008.springworld.cronkeeper;

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
	private Object result;
	private boolean approved;

	public JobPeerResult() {
	}

	public JobPeerResult(JobKey jobKey, Object attachment) {
		this.jobKey = jobKey;
		this.attachment = attachment;
	}

}
