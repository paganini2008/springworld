package com.github.paganini2008.springworld.cronkeeper.ui.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.paganini2008.springworld.cronkeeper.JacksonUtils;
import com.github.paganini2008.springworld.cronkeeper.JobState;
import com.github.paganini2008.springworld.cronkeeper.RunningState;
import com.github.paganini2008.springworld.cronkeeper.TriggerType;
import com.github.paganini2008.springworld.cronkeeper.model.TriggerDescription;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobDetail
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobDetail {

	private String clusterName;
	private String groupName;
	private String jobName;
	private String jobClassName;
	private int triggerType;
	private String triggerDescription;
	private int jobState;
	private int lastRunningState;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastExecutionTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastCompletionTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date nextExecutionTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date endDate;

	public String getTriggerType() {
		return TriggerType.valueOf(triggerType).getRepr();
	}

	public String getJobState() {
		return JobState.valueOf(jobState).getRepr();
	}

	public String getLastRunningState() {
		return RunningState.valueOf(lastRunningState).getRepr();
	}

	public TriggerDescription getTriggerDescription() {
		return JacksonUtils.parseJson(triggerDescription, TriggerDescription.class);
	}

}
