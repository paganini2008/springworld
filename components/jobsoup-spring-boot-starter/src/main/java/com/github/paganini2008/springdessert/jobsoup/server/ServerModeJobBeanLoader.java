package com.github.paganini2008.springdessert.jobsoup.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springdessert.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springdessert.jobsoup.Job;
import com.github.paganini2008.springdessert.jobsoup.JobBeanLoader;
import com.github.paganini2008.springdessert.jobsoup.JobKey;
import com.github.paganini2008.springdessert.jobsoup.JobManager;
import com.github.paganini2008.springdessert.jobsoup.model.JobTriggerDetail;

/**
 * 
 * ServerModeJobBeanLoader
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class ServerModeJobBeanLoader implements JobBeanLoader {

	@Autowired
	private JobManager jobManager;

	@Override
	public Job loadJobBean(JobKey jobKey) throws Exception {
		final JobTriggerDetail triggerDetail = jobManager.getJobTriggerDetail(jobKey);
		return ApplicationContextUtils.autowireBean(new ServerModeJobBeanProxy(jobKey, triggerDetail));

	}

}
