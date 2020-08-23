package com.github.paganini2008.springworld.myjob;

import java.io.Serializable;

import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobParam
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobParam implements Serializable {

	private static final long serialVersionUID = -1284831972153875870L;
	private JobKey jobKey;
	private Object attachment;

	public JobParam() {
	}

	public JobParam(JobKey jobKey, Object attachment) {
		this.jobKey = jobKey;
		this.attachment = attachment;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
