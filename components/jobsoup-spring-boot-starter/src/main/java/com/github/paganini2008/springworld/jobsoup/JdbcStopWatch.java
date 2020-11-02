package com.github.paganini2008.springworld.jobsoup;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.jdbc.ConnectionFactory;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.net.NetUtils;
import com.github.paganini2008.springworld.cluster.InstanceId;

/**
 * 
 * JdbcStopWatch
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JdbcStopWatch implements StopWatch {

	@Autowired
	private InstanceId instanceId;

	@Value("${server.port}")
	private int port;

	@Autowired
	private ConnectionFactory connectionFactory;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private JobFutureHolder jobFutureHolder;

	@Override
	public JobState onJobBegin(long traceId, JobKey jobKey, Date startDate) {
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
			long nextExecutionTime = jobFutureHolder.hasKey(jobKey)
					? jobFutureHolder.get(jobKey).getNextExectionTime(startDate, startDate, startDate)
					: -1L;
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNNING_BEGIN,
					new Object[] { JobState.RUNNING.getValue(), new Timestamp(startDate.getTime()),
							nextExecutionTime > 0 ? new Timestamp(nextExecutionTime) : null, jobKey.getClusterName(), jobKey.getGroupName(),
							jobKey.getJobName(), jobKey.getJobClassName() });
			return JobState.RUNNING;
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobState onJobEnd(long traceId, JobKey jobKey, Date startDate, RunningState runningState, int retries) {
		final int jobId = getJobId(jobKey);
		final Date endTime = new Date();
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
			connection.setAutoCommit(false);
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNNING_END,
					new Object[] { JobState.SCHEDULING.getValue(), runningState.getValue(), endTime, jobKey.getClusterName(),
							jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });
			int complete = 0, failed = 0, skipped = 0, finished = 0;
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
			case FINISHED:
				finished = 1;
				break;
			default:
				break;
			}
			JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_TRACE, new Object[] { traceId, jobId, runningState.getValue(),
					getSelfAddress(), instanceId.get(), complete, failed, skipped, finished, retries, startDate, endTime });
			connection.commit();
			return JobState.SCHEDULING;
		} catch (SQLException e) {
			JdbcUtils.rollbackQuietly(connection);
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}

	}

	protected String getSelfAddress() {
		return NetUtils.getLocalHost() + ":" + port;
	}

	private int getJobId(JobKey jobKey) {
		try {
			return jobManager.getJobId(jobKey);
		} catch (Exception e) {
			throw ExceptionUtils.wrapExeception(e);
		}
	}

}
