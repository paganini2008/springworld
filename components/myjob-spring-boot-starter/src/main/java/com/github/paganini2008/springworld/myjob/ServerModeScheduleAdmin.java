package com.github.paganini2008.springworld.myjob;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * ServerModeScheduleAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ServerModeScheduleAdmin implements ScheduleAdmin {

	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private ScheduleManager scheduleManager;

	@Override
	public JobState scheduleJob(JobKey jobKey) {
		try {
			Job job = jobBeanLoader.loadJobBean(jobKey);
			scheduleManager.schedule(job);
			return jobManager.getJobRuntime(jobKey).getJobState();
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	public JobState unscheduleJob(JobKey jobKey) {
		try {
			scheduleManager.unscheduleJob(jobKey);
			return jobManager.getJobRuntime(jobKey).getJobState();
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		}
	}

}
