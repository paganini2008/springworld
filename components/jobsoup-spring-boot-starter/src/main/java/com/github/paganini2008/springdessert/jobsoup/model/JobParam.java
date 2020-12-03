package com.github.paganini2008.springdessert.jobsoup.model;

import com.github.paganini2008.devtools.beans.ToStringBuilder;
import com.github.paganini2008.springdessert.jobsoup.JobKey;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobParam
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobParam {

	private JobKey jobKey;
	private Object attachment;
	private int retries;

	public JobParam() {
	}

	public JobParam(JobKey jobKey, Object attachment, int retries) {
		this.jobKey = jobKey;
		this.attachment = attachment;
		this.retries = retries;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
