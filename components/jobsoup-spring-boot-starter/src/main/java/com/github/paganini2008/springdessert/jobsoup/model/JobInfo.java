package com.github.paganini2008.springdessert.jobsoup.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.paganini2008.springdessert.jobsoup.JobState;
import com.github.paganini2008.springdessert.jobsoup.RunningState;
import com.github.paganini2008.springdessert.jobsoup.TriggerType;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobInfo
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobInfo implements Serializable {

	private static final long serialVersionUID = -1528742044603986153L;

	private int jobId;
	private String jobName;
	private String jobClassName;
	private String groupName;
	private TriggerType jobType;
	private String description;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createDate;
	private JobState jobState;
	private RunningState lastRunningState;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastExecutionTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastCompletionTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date nextExecutionTime;

}
