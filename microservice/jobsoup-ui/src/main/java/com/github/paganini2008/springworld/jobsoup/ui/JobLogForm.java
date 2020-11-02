package com.github.paganini2008.springworld.jobsoup.ui;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobLogForm
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobLogForm {

	private String jobKey;
	private long traceId;
	private String log;
	private String level;

}
