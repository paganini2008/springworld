package com.github.paganini2008.springworld.scheduler;

import com.github.paganini2008.devtools.collection.Tuple;

/**
 * 
 * JobConfig
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobConfig implements Job {

	private final Tuple config;

	public JobConfig(Tuple config) {
		this.config = config;
	}

	@Override
	public String getJobName() {
		return config.getProperty("jobName");
	}

	@Override
	public String getJobClassName() {
		return config.getProperty("jobClassName");
	}

	@Override
	public String getGroupName() {
		return config.getProperty("groupName");
	}

	public Tuple getConfig() {
		return config;
	}

	@Override
	public Object execute(Object result) {
		throw new UnsupportedOperationException();
	}

}
