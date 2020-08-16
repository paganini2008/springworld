package com.github.paganini2008.springworld.scheduler;

/**
 * 
 * ExternalJobBeanProxy
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ExternalJobBeanProxy implements Job {

	private final JobKey jobKey;
	private final Trigger trigger;

	public ExternalJobBeanProxy(JobKey jobKey, Trigger trigger) {
		this.jobKey = jobKey;
		this.trigger = trigger;
	}

	@Override
	public String getJobName() {
		return jobKey.getJobName();
	}

	@Override
	public String getJobClassName() {
		return jobKey.getJobClassName();
	}

	@Override
	public String getGroupName() {
		return jobKey.getGroupName();
	}

	@Override
	public Trigger getTrigger() {
		return trigger;
	}

	@Override
	public Object execute(JobKey jobKey, Object result) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean managedByApplicationContext() {
		return false;
	}
	
	

}
