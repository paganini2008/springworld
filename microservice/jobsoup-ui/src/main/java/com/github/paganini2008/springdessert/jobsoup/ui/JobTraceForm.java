package com.github.paganini2008.springdessert.jobsoup.ui;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobTraceForm
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobTraceForm {

	private String jobKey;
	private Date startDate;
	private Date endDate;

}
