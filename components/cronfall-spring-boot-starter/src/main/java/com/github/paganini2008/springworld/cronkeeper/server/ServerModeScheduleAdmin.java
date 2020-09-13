package com.github.paganini2008.springworld.cronkeeper.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.cronkeeper.Job;
import com.github.paganini2008.springworld.cronkeeper.JobBeanLoader;
import com.github.paganini2008.springworld.cronkeeper.JobException;
import com.github.paganini2008.springworld.cronkeeper.JobKey;
import com.github.paganini2008.springworld.cronkeeper.JobState;
import com.github.paganini2008.springworld.cronkeeper.ScheduleAdmin;
import com.github.paganini2008.springworld.cronkeeper.ScheduleManager;

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
