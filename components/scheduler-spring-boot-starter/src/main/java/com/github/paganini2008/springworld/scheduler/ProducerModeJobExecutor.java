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
	protected void beforeRun(Job job, Date startTime) {
		super.beforeRun(job, startTime);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			long nextExecutionTime = scheduleManager.getFuture(job).getNextExectionTime();
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNTIME_START, new Object[] { JobState.RUNNING.getValue(),
					new Timestamp(nextExecutionTime), startTime, job.getJobName(), job.getJobClassName() });
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
	protected boolean isRunning(Job job) {
		try {
			return jobManager.hasJobState(job, JobState.RUNNING);
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

}
