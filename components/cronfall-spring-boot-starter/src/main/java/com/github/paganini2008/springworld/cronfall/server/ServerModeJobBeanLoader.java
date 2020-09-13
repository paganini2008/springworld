package com.github.paganini2008.springworld.cronfall.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.cronfall.Job;
import com.github.paganini2008.springworld.cronfall.JobBeanLoader;
import com.github.paganini2008.springworld.cronfall.JobKey;
import com.github.paganini2008.springworld.cronfall.JobManager;
import com.github.paganini2008.springworld.cronfall.model.JobTriggerDetail;

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
