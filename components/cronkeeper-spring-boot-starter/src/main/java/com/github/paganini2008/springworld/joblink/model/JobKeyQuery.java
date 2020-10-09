package com.github.paganini2008.springworld.joblink.model;

import com.github.paganini2008.springworld.joblink.TriggerType;

import lombok.Data;

/**
 * 
 * JobKeyQuery
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Data
public class JobKeyQuery {

	private String clusterName;
	private TriggerType triggerType;

}
