package com.github.paganini2008.springworld.jobstorm.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobStat
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobStat implements Serializable {

	private static final long serialVersionUID = 5741263651318840914L;

	private int jobId;
	private int completeCount;
	private int skippedCount;
	private int failedCount;
	private String execution;

}
