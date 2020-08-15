package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
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
 * EmbeddedModeJobExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class EmbeddedModeJobExecutor extends JobTemplate implements JobExecutor {

	@Autowired
	private JobManager jobManager;

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JobDependencyObservable jobDependencyObservable;

	@Override
	protected void beforeRun(JobKey jobKey, Job job, Date startTime) {
		super.beforeRun(jobKey, job, startTime);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			long nextExecutionTime = scheduleManager.getJobFuture(jobKey).getNextExectionTime(startTime, startTime, startTime);
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNNING_BEGIN,
					new Object[] { JobState.RUNNING.getValue(), new Timestamp(startTime.getTime()), new Timestamp(nextExecutionTime),
							job.getGroupName(), job.getJobName(), job.getJobClassName() });
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
	protected void notifyDependencies(JobKey jobKey, Job job, Object result) {
		jobDependencyObservable.notifyDependencies(jobKey, result);
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
