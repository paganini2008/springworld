package com.github.paganini2008.springworld.jobstorm.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.jobstorm.Job;
import com.github.paganini2008.springworld.jobstorm.JobBeanLoader;
import com.github.paganini2008.springworld.jobstorm.JobKey;
import com.github.paganini2008.springworld.jobstorm.JobManager;
import com.github.paganini2008.springworld.jobstorm.model.JobTriggerDetail;

/**
 * 
 * ServerModeJobBeanLoader
 * 
 * @author Fred Feng
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