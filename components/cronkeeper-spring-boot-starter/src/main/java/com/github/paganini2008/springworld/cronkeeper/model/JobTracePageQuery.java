package com.github.paganini2008.springworld.cronkeeper.model;

import java.util.Date;

import com.github.paganini2008.springworld.cronkeeper.JobKey;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobTracePageQuery
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobTracePageQuery<T> extends PageQuery<T> {

	private JobKey jobKey;
	private Date startDate;
	private Date endDate;

	public JobTracePageQuery(JobKey jobKey) {
		this.jobKey = jobKey;
	}

}
