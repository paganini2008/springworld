package com.github.paganini2008.springworld.jobsoup.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobLog
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobLog implements Serializable {

	private static final long serialVersionUID = 681499736776643890L;
	private long traceId;
	private int jobId;
	private String level;
	private String log;
	private Date createDate;

}
