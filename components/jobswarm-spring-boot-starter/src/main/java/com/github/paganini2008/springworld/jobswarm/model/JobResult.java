package com.github.paganini2008.springworld.jobswarm.model;

import java.io.Serializable;

import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobResult
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobResult<T> implements Serializable {

	private static final long serialVersionUID = -6257798137365527003L;

	private boolean success;
	private String msg;
	private T data;

	public JobResult() {
	}
	
	public static <T> JobResult<T> success(T data){
		return success(data, "ok");
	}

	public static <T> JobResult<T> success(T data, String msg) {
		JobResult<T> jobResult = new JobResult<T>();
		jobResult.setSuccess(true);
		jobResult.setMsg(msg);
		jobResult.setData(data);
		return jobResult;
	}

	public static <T> JobResult<T> failure(String msg) {
		JobResult<T> jobResult = new JobResult<T>();
		jobResult.setSuccess(false);
		jobResult.setMsg(msg);
		return jobResult;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
