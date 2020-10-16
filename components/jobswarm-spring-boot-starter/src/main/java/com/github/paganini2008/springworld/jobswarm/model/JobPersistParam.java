package com.github.paganini2008.springworld.jobswarm.model;

import java.util.Date;

import com.github.paganini2008.springworld.jobswarm.JobKey;
import com.github.paganini2008.springworld.jobswarm.TriggerType;

import lombok.Data;

/**
 * 
 * JobPersistParam
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Data
public class JobPersistParam {

	private JobKey jobKey;

	private String description;
	private String email;
	private int retries;
	private long timeout = -1L;
	private int weight = 100;
	private String attachment;

	private TriggerType triggerType;
	private TriggerDescription triggerDescription;
	private Date startDate;
	private Date endDate;
	private int repeatCount = -1;

	public JobPersistParam() {
	}

	public JobPersistParam(String clusterName, String groupName, String jobName, String jobClassName) {
		this.jobKey = JobKey.by(clusterName, groupName, jobName, jobClassName);
	}

}
