package com.github.paganini2008.springworld.crontab.model;

import java.util.Date;

import com.github.paganini2008.springworld.crontab.TriggerType;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobPersistParam
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobPersistParam {

	private String clusterName;
	private String groupName;
	private String jobName;
	private String jobClassName;
	private TriggerType triggerType;
	private TriggerDescription triggerDescription;
	private Date startDate;
	private Date endDate;
	private String description;
	private String email;
	private int retries;
	private String attachment;

}
