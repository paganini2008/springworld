package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.jdbc.JdbcUtils;

/**
 * 
 * ProducerModeJobExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ProducerModeJobExecutor extends JobTemplate implements JobExecutor {

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private DataSource dataSource;

	@Override
	protected void beforeRun(JobKey jobKey, Job job, Date startTime) {
		super.beforeRun(jobKey, job, startTime);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			long nextExecutionTime = scheduleManager.getJobFuture(jobKey).getNextExectionTime(startTime, startTime, startTime);
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNNING_BEGIN, new Object[] { JobState.RUNNING.getValue(),
					new Timestamp(startTime.getTime()), new Timestamp(nextExecutionTime), job.getJobName(), job.getJobClassName() });
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public void execute(Job job, Object attachment) {
		runJob(job, attachment);
	}

	@Override
	protected boolean isScheduling(JobKey jobKey, Job job) {
		try {
			return jobManager.hasJobState(jobKey, JobState.SCHEDULING);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	protected void cancel(JobKey jobKey, Job job, RunningState runningState, Throwable reason) {
		scheduleManager.unscheduleJob(jobKey);

		try {
			jobManager.setJobState(jobKey, JobState.FINISHED);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
