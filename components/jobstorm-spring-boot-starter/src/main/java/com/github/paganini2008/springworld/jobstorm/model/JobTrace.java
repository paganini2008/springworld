package com.github.paganini2008.springworld.jobstorm.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.paganini2008.springworld.jobstorm.RunningState;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobTrace
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobTrace implements Serializable {

	private static final long serialVersionUID = 1886119510627026178L;
	private long traceId;
	private String address;
	private String instanceId;
	private RunningState runningState;
	private int completed;
	private int failed;
	private int skipped;
	private int retries;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date executionTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date completionTime;

}
