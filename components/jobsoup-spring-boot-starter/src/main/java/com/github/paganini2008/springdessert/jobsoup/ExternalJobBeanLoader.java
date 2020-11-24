package com.github.paganini2008.springdessert.jobsoup;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springdessert.jobsoup.model.JobTriggerDetail;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

/**
 * 
 * ExternalJobBeanLoader
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ExternalJobBeanLoader implements JobBeanLoader {

	@Autowired
	private JobManager jobManager;

	@Override
	public Job loadJobBean(JobKey jobKey) throws Exception {
		final JobTriggerDetail triggerDetail = jobManager.getJobTriggerDetail(jobKey);
		return ApplicationContextUtils.autowireBean(new ExternalJobBeanProxy(jobKey, triggerDetail));
	}

}
