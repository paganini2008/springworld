package com.github.paganini2008.springdessert.jobsoup;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.beans.PropertyFilters;
import com.github.paganini2008.devtools.beans.ToStringBuilder;

/**
 * 
 * ManagedJob
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public abstract class ManagedJob implements Job, BeanNameAware {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Value("${spring.application.name}")
	private String applicationName;

	private String jobName;

	@Override
	public String getClusterName() {
		return clusterName;
	}

	@Override
	public String getGroupName() {
		return applicationName;
	}

	@Override
	public String getJobName() {
		return jobName;
	}

	@Override
	public void setBeanName(String name) {
		this.jobName = name;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				PropertyFilters.includedProperties(new String[] { "clusterName", "groupName", "jobName", "jobClassName" }));
	}

}
