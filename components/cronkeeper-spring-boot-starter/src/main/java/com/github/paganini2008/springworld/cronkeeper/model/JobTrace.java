package com.github.paganini2008.springworld.cronkeeper.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.paganini2008.springworld.cronkeeper.RunningState;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobTrace
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobTrace implements Serializable {

	private static final long serialVersionUID = 1886119510627026178L;
	private int traceId;
	private int runningState;
	private int complete;
	private int failed;
	private int skipped;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date executionTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date completionTime;
	private String address;
	private String instanceId;

	public String getRunningState() {
		return RunningState.valueOf(runningState).getRepr();
	}

}
