package com.github.paganini2008.springworld.cronkeeper.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobStackTrace
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobStackTrace implements Serializable {

	private static final long serialVersionUID = -1363956445864067818L;

	private int traceId;
	private int jobId;
	private String stackTrace;

}