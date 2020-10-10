package com.github.paganini2008.springworld.jobclick.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.jobclick.Job;
import com.github.paganini2008.springworld.jobclick.JobBeanLoader;
import com.github.paganini2008.springworld.jobclick.JobException;
import com.github.paganini2008.springworld.jobclick.JobKey;
import com.github.paganini2008.springworld.jobclick.JobState;
import com.github.paganini2008.springworld.jobclick.ScheduleAdmin;
import com.github.paganini2008.springworld.jobclick.ScheduleManager;

/**
 * 
 * ServerModeScheduleAdmin
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ServerModeScheduleAdmin implements ScheduleAdmin{

	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Autowired
	private ScheduleManager scheduleManager;

	@Override
	public JobState scheduleJob(JobKey jobKey) {
		try {
			Job job = jobBeanLoader.loadJobBean(jobKey);
			return scheduleManager.schedule(job);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public JobState unscheduleJob(JobKey jobKey) {
		try {
			return scheduleManager.unscheduleJob(jobKey);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}
	
}
