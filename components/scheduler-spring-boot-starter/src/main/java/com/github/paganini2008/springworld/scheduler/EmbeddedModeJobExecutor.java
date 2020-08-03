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
 * EmbeddedModeJobExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class EmbeddedModeJobExecutor extends ServerModeJobExecutor {

	@Autowired
	private ScheduleManager scheduleManager;

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

}
