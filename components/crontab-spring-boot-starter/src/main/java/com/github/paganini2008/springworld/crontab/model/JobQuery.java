package com.github.paganini2008.springworld.crontab.model;

import com.github.paganini2008.springworld.crontab.TriggerType;

import lombok.Data;

/**
 * 
 * JobQuery
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Data
public class JobQuery {

	private String clusterName;
	private TriggerType triggerType;

}
