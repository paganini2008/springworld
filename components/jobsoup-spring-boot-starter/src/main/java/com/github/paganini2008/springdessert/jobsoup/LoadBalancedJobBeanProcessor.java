package com.github.paganini2008.springdessert.jobsoup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springdessert.cluster.Constants;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMessageListener;
import com.github.paganini2008.springdessert.jobsoup.model.JobParam;

/**
 * 
 * LoadBalancedJobBeanProcessor
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class LoadBalancedJobBeanProcessor implements ApplicationMessageListener {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Qualifier(BeanNames.TARGET_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Qualifier(BeanNames.INTERNAL_JOB_BEAN_LOADER)
	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		JobParam jobParam = (JobParam) message;
		Job job;
		try {
			job = jobBeanLoader.loadJobBean(jobParam.getJobKey());
		} catch (Exception e) {
			throw ExceptionUtils.wrapExeception(e);
		}
		jobExecutor.execute(job, jobParam.getAttachment(), jobParam.getRetries());
	}

	@Override
	public String getTopic() {
		return Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:loadbalance";
	}

}
