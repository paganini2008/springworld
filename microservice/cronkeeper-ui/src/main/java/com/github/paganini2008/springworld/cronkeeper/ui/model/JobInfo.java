package com.github.paganini2008.springworld.cronkeeper.ui.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.paganini2008.springworld.cronkeeper.JobState;
import com.github.paganini2008.springworld.cronkeeper.TriggerType;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobInfo
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobInfo {

	private int jobId;
	private String clusterName;
	private String groupName;
	private String jobName;
	private String jobClassName;
	private String description;
	private String attachment;
	private String email;
	private int retries;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createDate;
	private int triggerType;
	private int jobState;

	public String getTriggerType() {
		return TriggerType.valueOf(triggerType).getRepr();
	}

	public String getJobState() {
		return JobState.valueOf(jobState).getRepr();
	}

}
