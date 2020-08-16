package com.github.paganini2008.springworld.scheduler;

import org.springframework.beans.factory.annotation.Autowired;

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
		TriggerType triggerType = triggerDetail.getTriggerType();
		TriggerDescription triggerDescription = triggerDetail.getTriggerDescription();
		return ApplicationContextUtils.autowireBean(new ExternalJobBeanProxy(jobKey, triggerType.getTrigger(triggerDescription)));
	}

}
