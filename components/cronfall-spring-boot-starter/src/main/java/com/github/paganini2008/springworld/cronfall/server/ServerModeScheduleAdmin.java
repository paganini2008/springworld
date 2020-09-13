package com.github.paganini2008.springworld.cronfall.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cronfall.Job;
import com.github.paganini2008.springworld.cronfall.JobBeanLoader;
import com.github.paganini2008.springworld.cronfall.JobException;
import com.github.paganini2008.springworld.cronfall.JobKey;
import com.github.paganini2008.springworld.cronfall.JobState;
import com.github.paganini2008.springworld.cronfall.ScheduleAdmin;
import com.github.paganini2008.springworld.cronfall.ScheduleManager;

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
