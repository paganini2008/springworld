package com.github.paganini2008.springworld.myjob;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

/**
 * 
 * ConsumerModeLoadBalancer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsumerModeLoadBalancer extends JobTemplate implements JobExecutor {

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JobFutureHolder jobFutureHolder;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Override
	public void execute(Job job, Object attachment) {
		runJob(job, attachment);
	}

	@Override
	protected void beforeRun(JobKey jobKey, Job job, Date startTime) {
		super.beforeRun(jobKey, job, startTime);
		handleIfDependentJob(jobKey, startTime);
	}

	private void handleIfDependentJob(JobKey jobKey, Date startTime) {
		if (jobFutureHolder.get(jobKey) instanceof JobDependencyFuture) {
			final JobFuture future = jobFutureHolder.get(jobKey);
			Connection connection = null;
			try {
				connection = dataSource.getConnection();
				long nextExecutionTime = future.getNextExectionTime(startTime, startTime, startTime);
				JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNNING_BEGIN,
						new Object[] { JobState.RUNNING.getValue(), new Timestamp(startTime.getTime()),
								nextExecutionTime > 0 ? new Timestamp(nextExecutionTime) : null, jobKey.getGroupName(), jobKey.getJobName(),
								jobKey.getJobClassName() });
			} catch (SQLException e) {
				throw new JobException(e.getMessage(), e);
			} finally {
				JdbcUtils.closeQuietly(connection);
			}
		}
	}

	@Override
	protected final RunningState doRun(JobKey jobKey, Job job, Object attachment) {
		if (clusterMulticastGroup.countOfChannel(jobKey.getGroupName()) > 0) {
			final String topic = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:loadbalance";
			clusterMulticastGroup.unicast(jobKey.getGroupName(), topic, new JobParam(jobKey, attachment));
		} else {
			try {
				jobManager.setJobState(jobKey, JobState.SCHEDULING);
			} catch (SQLException e) {
				throw new JobException(e.getMessage(), e);
			}
		}
		return RunningState.RUNNING;
	}

	@Override
	protected boolean isScheduling(JobKey jobKey, Job job) {
		return true;
	}

}
