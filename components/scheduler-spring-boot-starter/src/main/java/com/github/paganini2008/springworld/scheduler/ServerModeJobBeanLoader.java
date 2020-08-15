package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

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
		JobTriggerDetail triggerDetail = jobManager.getJobTriggerDetail(jobKey);
		TriggerType triggerType = triggerDetail.getTriggerType();
		TriggerDescription triggerDescription = triggerDetail.getTriggerDescription();
		return ApplicationContextUtils.autowireBean(new ServerModeJobBeanProxy(jobKey, triggerType.getTrigger(triggerDescription)));

	}

}
