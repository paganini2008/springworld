package com.github.paganini2008.springdessert.jobsoup.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springdessert.jobsoup.Job;
import com.github.paganini2008.springdessert.jobsoup.JobBeanLoader;
import com.github.paganini2008.springdessert.jobsoup.JobException;
import com.github.paganini2008.springdessert.jobsoup.JobKey;
import com.github.paganini2008.springdessert.jobsoup.JobState;
import com.github.paganini2008.springdessert.jobsoup.ScheduleAdmin;
import com.github.paganini2008.springdessert.jobsoup.ScheduleManager;

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
