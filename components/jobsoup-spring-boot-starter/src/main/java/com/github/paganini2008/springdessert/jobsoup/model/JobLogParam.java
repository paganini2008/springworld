package com.github.paganini2008.springdessert.jobsoup.model;

import com.github.paganini2008.springdessert.jobsoup.JobKey;
import com.github.paganini2008.springdessert.jobsoup.LogLevel;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobLogParam
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobLogParam {

	private long traceId;
	private JobKey jobKey;
	private LogLevel logLevel;
	private String messagePattern;
	private Object[] args;
	private String[] stackTraces;

	public JobLogParam() {
	}

	public JobLogParam(long traceId, JobKey jobKey, LogLevel logLevel, String messagePattern, Object[] args, String[] stackTraces) {
		this.traceId = traceId;
		this.jobKey = jobKey;
		this.logLevel = logLevel;
		this.messagePattern = messagePattern;
		this.args = args;
		this.stackTraces = stackTraces;
	}

	public JobLogParam(long traceId, JobKey jobKey, String[] stackTraces) {
		this.traceId = traceId;
		this.jobKey = jobKey;
		this.stackTraces = stackTraces;
	}

}
