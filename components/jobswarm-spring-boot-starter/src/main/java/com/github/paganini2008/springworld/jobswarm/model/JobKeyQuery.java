package com.github.paganini2008.springworld.jobswarm.model;

import com.github.paganini2008.springworld.jobswarm.TriggerType;

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
