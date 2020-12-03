package com.github.paganini2008.springdessert.jobsoup.ui;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobLogForm
 * 
 * @author Jimmy Hoff
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
