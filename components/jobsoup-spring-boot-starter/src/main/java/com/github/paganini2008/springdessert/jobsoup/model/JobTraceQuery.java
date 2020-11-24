package com.github.paganini2008.springdessert.jobsoup.model;

import com.github.paganini2008.springdessert.jobsoup.JobKey;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobTraceQuery
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobTraceQuery extends Query {

	private JobKey jobKey;
	private long traceId;

	public JobTraceQuery() {
	}

	public JobTraceQuery(JobKey jobKey) {
		this.jobKey = jobKey;
	}

	public JobTraceQuery(JobKey jobKey, long traceId) {
		this.jobKey = jobKey;
		this.traceId = traceId;
	}

}
