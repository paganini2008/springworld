package com.github.paganini2008.springworld.scheduler;

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
	
	public static void main(String[] args) {
		JobParam jobParam = new JobParam();
		jobParam.setJobKey(JobKey.of("tester:healthCheckJob@com.allyes.springboot.tester.job.HealthCheckJob"));
		jobParam.setAttachment("Hello world!");
		String json = JacksonUtils.toJsonString(jobParam);
		System.out.println(json);
		System.out.println(JacksonUtils.parseJson(json, JobParam.class));
	}

}
