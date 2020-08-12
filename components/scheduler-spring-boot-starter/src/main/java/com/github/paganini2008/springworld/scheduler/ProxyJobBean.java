package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * ProxyJobBean
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ProxyJobBean implements Job {

	private final JobConfig jobConfig;

	ProxyJobBean(JobConfig jobConfig) {
		this.jobConfig = jobConfig;
	}

	@Override
	public String getJobName() {
		return jobConfig.getJobName();
	}

	@Override
	public String getJobClassName() {
		return jobConfig.getJobClassName();
	}

	@Override
	public String getDescription() {
		return jobConfig.getDescription();
	}

	@Override
	public int getRetries() {
		return jobConfig.getRetries();
	}

	@Override
	public String getEmail() {
		return jobConfig.getEmail();
	}

	@Override
	public String getGroupName() {
		return jobConfig.getGroupName();
	}

	@Override
	public Object execute(Object result) {
		throw new UnsupportedOperationException();
	}

}
