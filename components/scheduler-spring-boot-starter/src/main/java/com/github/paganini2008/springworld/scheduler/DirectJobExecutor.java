package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.collection.Tuple;
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
	private JobManager jobManager;

	@Autowired
	private JobDependency jobDependency;

	@Autowired
	private JobStore jobStore;

	@Autowired
	private DataSource dataSource;

	@Override
	public void execute(Job job, Object arg) {
		runJob(job, arg);
	}

	@Override
	protected boolean isRunning(Job job) {
		try {
			return jobStore.getJobRuntime(job).getJobState() == JobState.RUNNING;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	protected void notifyDependencies(Job job, Object result) {
		jobDependency.notifyDependencies(job, result);
	}

	@Override
	protected void beforeRun(Job job, Date startTime) {
		super.beforeRun(job, startTime);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNTIME_START,
					new Object[] { JobState.RUNNING.getValue(), startTime, job.getJobName(), job.getJobClassName() });
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	protected void afterRun(Job job, Date startTime, RunningState runningState, Throwable reason) {
		super.afterRun(job, startTime, runningState, reason);
		final Date endTime = new Date();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			long nextExecutionTime = jobManager.getFuture(job).getNextExectionTime();
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNTIME_END, new Object[] { JobState.SCHEDULING.getValue(),
					runningState.getValue(), endTime, new Date(nextExecutionTime), job.getJobName(), job.getJobClassName() });

			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_DETAIL,
					new Object[] { job.getJobName(), job.getJobClassName() });
			int jobId = (Integer) tuple.get("jobId");
			int complete = 0, failed = 0, skipped = 0;
			switch (runningState) {
			case COMPLETED:
				complete = 1;
				break;
			case FAILED:
				failed = 1;
				break;
			case SKIPPED:
				skipped = 1;
				break;
			}

			int traceId = JdbcUtils.insert(connection, SqlScripts.DEF_INSERT_JOB_TRACE,
					new Object[] { jobId, runningState.getValue(), complete, failed, skipped, startTime, endTime });

			if (reason != null) {
				String[] traces = ExceptionUtils.toArray(reason);
				List<Object[]> argsList = new ArrayList<Object[]>();
				for (String trace : traces) {
					argsList.add(new Object[] { traceId, jobId, trace });
				}
				JdbcUtils.batchUpdate(connection, SqlScripts.DEF_INSERT_JOB_EXCEPTION, argsList);
			}
			connection.commit();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			JdbcUtils.rollbackQuietly(connection);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}

	}

}
