package com.github.paganini2008.springworld.scheduler;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * JobInfo
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Setter
@Getter
@ToString
public class JobInfo implements Serializable {

	private static final long serialVersionUID = 5741263651318840914L;

	private String jobName;
	private String jobClass;
	private String description;
	private JobState jobState;
	private RunningState lastRunningState;
	private int completedCount;
	private int skippedCount;
	private int failedCount;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastExecutionTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date nextExecutionTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createDate;

}
