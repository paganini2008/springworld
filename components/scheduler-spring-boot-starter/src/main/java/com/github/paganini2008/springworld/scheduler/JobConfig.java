package com.github.paganini2008.springworld.scheduler;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobConfig
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobConfig {

	private String jobName;
	private String jobClassName;
	private String groupName;
	private String cron;
	private TriggerType triggerType;
	private TriggerDescription triggerDescription;
	private String description;
	private String email;
	private int retries;

}
