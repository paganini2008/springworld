package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.jdbc.JdbcUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DirectJobExecutor
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class DirectJobExecutor extends JobTemplate implements JobExecutor {

	@Autowired
	private JobExecutionContext context;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private DataSource dataSource;

	@Override
	public void execute(Job job, Object arg) {
		runJob(job, arg);
	}

	@Override
	protected boolean isRunning(Job job) {
		try {
			return context.getJobRuntime(job).getJobState() == JobState.RUNNING;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	protected void beforeRun(Job job) {
		super.beforeRun(job);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_PANEL_WHEN_START,
					new Object[] { JobState.RUNNING.getValue(), System.currentTimeMillis(), job.getJobName() });
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	protected void afterRun(Job job, RunningState runningState) {
		super.afterRun(job, runningState);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			int completed = 0, skipped = 0, failed = 0;
			switch (runningState) {
			case COMPLETED:
				completed = 1;
				break;
			case SKIPPED:
				skipped = 1;
				break;
			case FAILED:
				failed = 1;
				break;
			}
			long now = System.currentTimeMillis();
			long nextExecutionTime = jobManager.getFuture(job).getNextExectionTime();
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_PANEL_WHEN_END, new Object[] { JobState.SCHEDULING.getValue(),
					runningState.getValue(), completed, skipped, failed, now, nextExecutionTime, job.getJobName() });
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

}
