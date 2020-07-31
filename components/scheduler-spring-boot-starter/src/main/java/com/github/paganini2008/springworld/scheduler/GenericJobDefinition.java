package com.github.paganini2008.springworld.scheduler;

import com.github.paganini2008.devtools.collection.Tuple;

import lombok.Setter;

/**
 * 
 * GenericJobDefinition
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Setter
public class GenericJobDefinition implements JobDefinition {

	private final String jobName;
	private final String jobClassName;
	private final String groupName;
	private String description;
	private int retries = 0;
	private String attachment;

	@Override
	public String getJobName() {
		return jobName;
	}

	@Override
	public String getJobClassName() {
		return jobClassName;
	}

	@Override
	public String getGroupName() {
		return groupName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getRetries() {
		return retries;
	}

	@Override
	public String getAttachment() {
		return attachment;
	}

	public GenericJobDefinition(String jobName, String jobClassName, String groupName) {
		this.jobName = jobName;
		this.jobClassName = jobClassName;
		this.groupName = groupName;
	}

	public static JobDefinition load(Tuple tuple) {
		GenericJobDefinition jobDefinition = new GenericJobDefinition(tuple.getProperty("jobName"), tuple.getProperty("jobClassName"),
				tuple.getProperty("groupName"));
		jobDefinition.setDescription(tuple.getProperty("description"));
		jobDefinition.setRetries(tuple.getProperty("retries", int.class));
		jobDefinition.setAttachment(tuple.getProperty("attachment"));
		return jobDefinition;
	}

}
