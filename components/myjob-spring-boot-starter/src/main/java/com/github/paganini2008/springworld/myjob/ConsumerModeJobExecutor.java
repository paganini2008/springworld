package com.github.paganini2008.springworld.myjob;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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

	@Qualifier(BeanNames.DATA_SOURCE)
	@Autowired
	private DataSource dataSource;

	@Autowired
	private JobManager jobManager;

	@Override
	public void execute(Job job, Object attachment) {
		runJob(job, attachment);
	}

	@Override
	protected boolean isScheduling(JobKey jobKey, Job job) {
		return true;
	}

	@Override
	protected void notifyDependants(JobKey jobKey, Job job, Object result) {
		try {
			if (jobManager.hasDependencies(jobKey)) {
				jobDependencyObservable.notifyDependants(jobKey, result);
			}
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		}
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
					runningState.getValue(), endTime, jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });

			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_DETAIL,
					new Object[] { jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });
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
			JdbcUtils.rollbackQuietly(connection);
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}

	}
}
