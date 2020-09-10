package com.github.paganini2008.springworld.crontab;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ArrayUtils;
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

	@Qualifier(BeanNames.DATA_SOURCE)
	@Autowired
	private DataSource dataSource;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private JobFutureHolder jobFutureHolder;

	@Override
	public JobState startJob(JobKey jobKey, Date startTime) {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			long nextExecutionTime = jobFutureHolder.hasKey(jobKey)
					? jobFutureHolder.get(jobKey).getNextExectionTime(startTime, startTime, startTime)
					: -1L;
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNNING_BEGIN,
					new Object[] { JobState.RUNNING.getValue(), new Timestamp(startTime.getTime()),
							nextExecutionTime > 0 ? new Timestamp(nextExecutionTime) : null, jobKey.getGroupName(), jobKey.getJobName(),
							jobKey.getJobClassName() });
			return JobState.RUNNING;
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobState finishJob(JobKey jobKey, Date startTime, RunningState runningState, String[] errorStackTracks, int retries) {
		final Date endTime = new Date();
		Connection connection = null;
		try {
			int jobId = jobManager.getJobId(jobKey);
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNNING_END, new Object[] { JobState.SCHEDULING.getValue(),
					runningState.getValue(), endTime, jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });
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

			int traceId = JdbcUtils.insert(connection, SqlScripts.DEF_INSERT_JOB_TRACE, new Object[] { jobId, runningState.getValue(),
					getSelfAddress(), instanceId.get(), complete, failed, skipped, retries, startTime, endTime });

			if (ArrayUtils.isNotEmpty(errorStackTracks)) {
				List<Object[]> argsList = new ArrayList<Object[]>();
				for (String errorStackTrack : errorStackTracks) {
					argsList.add(new Object[] { traceId, jobId, errorStackTrack });
				}
				JdbcUtils.batchUpdate(connection, SqlScripts.DEF_INSERT_JOB_EXCEPTION, argsList);
			}
			connection.commit();
			return JobState.SCHEDULING;
		} catch (Exception e) {
			JdbcUtils.rollbackQuietly(connection);
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}

	}

	protected String getSelfAddress() {
		return NetUtils.getLocalHost() + ":" + port;
	}

}
