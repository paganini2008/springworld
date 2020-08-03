package com.github.paganini2008.springworld.scheduler;

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
 * LoadBalancedJobExecutor
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class LoadBalancedJobExecutor extends JobTemplate implements JobExecutor {

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired
	private DataSource dataSource;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Override
	public void execute(Job job, Object attachment) {
		runJob(job, attachment);
	}

	@Override
	protected final void beforeRun(Job job, Date startTime) {
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
	protected final RunningState doRun(Job job, Object attachment) {
		final String topic = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:loadbalance";
		clusterMulticastGroup.unicast(job.getGroupName(), topic,
				new JobParameter(job.getSignature(), job.getGroupName(), job.getJobName(), job.getJobClassName(), attachment));
		return RunningState.RUNNING;
	}

	@Override
	protected boolean isRunning(Job job) {
		try {
			return jobManager.getJobRuntime(job).getJobState() == JobState.RUNNING;
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

}
