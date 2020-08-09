package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JdbcJobManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JdbcJobManager implements JobManager {

	private static final Map<String, String> ddls = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		{
			put("cluster_job_detail", SqlScripts.DEF_DDL_JOB_DETAIL);
			put("cluster_job_trigger", SqlScripts.DEF_DDL_JOB_TRIGGER);
			put("cluster_job_runtime", SqlScripts.DEF_DDL_JOB_RUNTIME);
			put("cluster_job_trace", SqlScripts.DEF_DDL_JOB_TRACE);
			put("cluster_job_exception", SqlScripts.DEF_DDL_JOB_EXCEPTION);
		}
	};

	@Autowired
	private DataSource dataSource;

	@Value("${spring.application.cluster.scheduler.createTable:true}")
	private boolean createTable;

	@Override
	public void configure() throws SQLException {
		if (!createTable) {
			return;
		}
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			for (Map.Entry<String, String> entry : new HashMap<String, String>(ddls).entrySet()) {
				if (!JdbcUtils.existsTable(connection, null, entry.getKey())) {
					JdbcUtils.update(connection, entry.getValue());
					log.info("Create table: " + entry.getKey());
				}
			}
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public void pauseJob(Job job) throws SQLException {
		if (hasJob(job) && hasJobState(job, JobState.SCHEDULING)) {
			try {
				setJobState(job, JobState.PAUSED);
				if (log.isTraceEnabled()) {
					log.trace("Pause the job: " + job.getSignature());
				}
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
		}
	}

	@Override
	public void resumeJob(Job job) throws SQLException {
		if (hasJob(job) && hasJobState(job, JobState.PAUSED)) {
			try {
				setJobState(job, JobState.SCHEDULING);
				if (log.isTraceEnabled()) {
					log.trace("Pause the job: " + job.getSignature());
				}
			} catch (Exception e) {
				throw new JobException(e.getMessage(), e);
			}
		}
	}

	@Override
	public int addJob(Job job, String attachment) throws SQLException {
		checkJobSignature(job);
		if (hasJob(job)) {
			return 0;
		}
		int id;
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			id = JdbcUtils.insert(connection, SqlScripts.DEF_INSERT_JOB_DETAIL, ps -> {
				ps.setString(1, job.getSignature());
				ps.setString(2, job.getJobName());
				ps.setString(3, job.getJobClassName());
				ps.setString(4, job.getGroupName());
				ps.setString(5, job.getDescription());
				ps.setString(6, attachment);
				ps.setString(7, job.getEmail());
				ps.setInt(8, job.getRetries());
				ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
			});

			JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_RUNTIME, ps -> {
				ps.setInt(1, id);
				ps.setInt(2, JobState.NOT_SCHEDULED.getValue());
			});

			JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_TRIGGER, ps -> {
				ps.setInt(1, id);
				ps.setInt(2, TriggerType.valueOf(job).getValue());
				ps.setString(3, getTriggerDescription(job));
			});

			connection.commit();
			log.info("Add job '" + job.getSignature() + "' ok.");
			return id;
		} catch (SQLException e) {
			JdbcUtils.rollbackQuietly(connection);
			throw e;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}

	}

	private String getTriggerDescription(Job job) {
		TriggerDescription data = new TriggerDescription();
		if (job instanceof CronJob) {
			data.setCron(((CronJob) job).getCronExpression());
		} else if (job instanceof PeriodicJob) {
			PeriodicJob periodicJob = (PeriodicJob) job;
			data.setDelay(periodicJob.getDelay());
			data.setDelaySchedulingUnit(periodicJob.getDelaySchedulingUnit());
			data.setPeriod(periodicJob.getPeriod());
			data.setPeriodSchedulingUnit(periodicJob.getPeriodSchedulingUnit());
			data.setSchedulingMode(periodicJob.getSchedulingMode());
		} else if (job instanceof SerialJob) {
			data.setDependencies(((SerialJob) job).getDependencies());
		} else {
			throw new IllegalStateException("Unknown job class: " + job.getClass());
		}
		return JacksonUtils.toJsonString(data);
	}

	@Override
	public void deleteJob(Job job) throws SQLException {
		checkJobSignature(job);
		if (!hasJob(job)) {
			throw new JobBeanNotFoundException(job.getSignature());
		}
		if (!hasJobState(job, JobState.NOT_SCHEDULED)) {
			throw new JobException("Please unschedule the job before you delete it.");
		}
		setJobState(job, JobState.FINISHED);
	}

	@Override
	public boolean hasJob(Job job) throws SQLException {
		checkJobSignature(job);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Integer result = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_NAME_EXISTS,
					new Object[] { job.getJobName(), job.getJobClassName() }, Integer.class);
			return result != null && result.intValue() > 0;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public void setJobState(Job job, JobState jobState) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_STATE,
					new Object[] { jobState.getValue(), job.getJobName(), job.getJobClassName() });
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public boolean hasJobState(Job job, JobState jobState) throws SQLException {
		JobRuntime jobRuntime = getJobRuntime(job);
		return jobRuntime.getJobState() == jobState;
	}

	@Override
	public JobDetail getJobDetail(Job job) throws Exception {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_DETAIL,
					new Object[] { job.getJobName(), job.getJobClassName() });
			return tuple.toBean(JobDetail.class);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobTrigger getJobTrigger(Job job) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_TRIGGER,
					new Object[] { job.getJobName(), job.getJobClassName() });
			return tuple.toBean(JobTrigger.class);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobRuntime getJobRuntime(Job job) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_RUNTIME,
					new Object[] { job.getJobName(), job.getJobClassName() });
			return tuple.toBean(JobRuntime.class);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobStat getJobStat(Job job) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_STAT,
					new Object[] { job.getJobName(), job.getJobClassName() });
			return tuple.toBean(JobStat.class);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public ResultSetSlice<JobInfo> getJobInfo() {
		final ResultSetSlice<Tuple> delegate = JdbcUtils.pageableQuery(dataSource, SqlScripts.DEF_SELECT_JOB_INFO, (Object[]) null);
		return new ResultSetSlice<JobInfo>() {

			@Override
			public int totalCount() {
				return delegate.totalCount();
			}

			@Override
			public List<JobInfo> list(int maxResults, int firstResult) {
				List<JobInfo> dataList = new ArrayList<JobInfo>(maxResults);
				for (Tuple tuple : delegate.list(maxResults, firstResult)) {
					JobInfo jobInfo = tuple.toBean(JobInfo.class);
					dataList.add(jobInfo);
				}
				return dataList;
			}

		};
	}

	@Override
	public ResultSetSlice<JobStat> getJobStat(StatType statType) throws SQLException {
		String extraColumns = "", extraGroupingColumns = "";
		if (statType != null) {
			extraColumns = ", " + statType.getExtraColumns();
			extraGroupingColumns = ", " + statType.getExtraGroupingColumns();
		}
		String sql = String.format(SqlScripts.DEF_SELECT_ALL_JOB_STAT, new Object[] { extraColumns, extraGroupingColumns });
		final ResultSetSlice<Tuple> delegate = JdbcUtils.pageableQuery(dataSource, sql, (Object[]) null);
		return new ResultSetSlice<JobStat>() {

			@Override
			public int totalCount() {
				return delegate.totalCount();
			}

			@Override
			public List<JobStat> list(int maxResults, int firstResult) {
				List<JobStat> dataList = new ArrayList<JobStat>(maxResults);
				for (Tuple tuple : delegate.list(maxResults, firstResult)) {
					JobStat jobStat = tuple.toBean(JobStat.class);
					dataList.add(jobStat);
				}
				return dataList;
			}

		};
	}

	private static void checkJobSignature(Job job) {
		if (StringUtils.isBlank(job.getJobName()) || StringUtils.isBlank(job.getJobClassName())) {
			throw new JobException("Invalid job signature: " + job.getSignature());
		}
	}

}
