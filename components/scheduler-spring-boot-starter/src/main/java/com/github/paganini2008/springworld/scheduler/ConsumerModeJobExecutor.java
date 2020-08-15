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

/**
 * 
 * ConsumerModeJobExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsumerModeJobExecutor extends JobTemplate implements JobExecutor {

	@Autowired
	private JobDependencyObservable jobDependencyObservable;

	@Autowired
	private DataSource dataSource;

	@Override
	public void execute(Job job, Object attachment) {
		runJob(job, attachment);
	}

	@Override
	protected boolean isScheduling(JobKey jobKey, Job job) {
		return true;
	}

	@Override
	protected void notifyDependencies(JobKey jobKey, Job job, Object result) {
		jobDependencyObservable.notifyDependencies(jobKey, result);
	}

	@Override
	protected void afterRun(JobKey jobKey, Job job, Date startTime, RunningState runningState, Throwable reason) {
		super.afterRun(jobKey, job, startTime, runningState, reason);
		final Date endTime = new Date();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNNING_END, new Object[] { JobState.SCHEDULING.getValue(),
					runningState.getValue(), endTime, job.getJobName(), job.getJobClassName() });

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
			default:
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
