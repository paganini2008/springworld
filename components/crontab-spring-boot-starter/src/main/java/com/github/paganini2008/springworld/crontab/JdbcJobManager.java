package com.github.paganini2008.springworld.crontab;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.Observer;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springworld.crontab.model.JobDetail;
import com.github.paganini2008.springworld.crontab.model.JobInfo;
import com.github.paganini2008.springworld.crontab.model.JobQuery;
import com.github.paganini2008.springworld.crontab.model.JobRuntime;
import com.github.paganini2008.springworld.crontab.model.JobStat;
import com.github.paganini2008.springworld.crontab.model.JobTriggerDetail;
import com.github.paganini2008.springworld.crontab.model.TriggerDescription.Serial;

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
			put("crontab_server_detail", SqlScripts.DEF_DDL_CLUSTER_DETAIL);
			put("crontab_job_detail", SqlScripts.DEF_DDL_JOB_DETAIL);
			put("crontab_job_trigger", SqlScripts.DEF_DDL_JOB_TRIGGER);
			put("crontab_job_runtime", SqlScripts.DEF_DDL_JOB_RUNTIME);
			put("crontab_job_trace", SqlScripts.DEF_DDL_JOB_TRACE);
			put("crontab_job_exception", SqlScripts.DEF_DDL_JOB_EXCEPTION);
			put("crontab_job_dependency", SqlScripts.DEF_DDL_JOB_DEPENDENCY);
		}
	};

	@Qualifier(BeanNames.DATA_SOURCE)
	@Autowired
	private DataSource dataSource;

	@Value("${spring.application.cluster.scheduler.createTable:true}")
	private boolean createTable;

	@Autowired
	private JobListenerContainer jobListenerContainer;

	@Override
	public void configure() throws SQLException {
		if (createTable) {
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

		jobListenerContainer.addListener(new NewJobCreationListener());
	}

	@Override
	public JobState pauseJob(JobKey jobKey) throws SQLException {
		if (hasJob(jobKey) && hasJobState(jobKey, JobState.SCHEDULING)) {
			if (log.isTraceEnabled()) {
				log.trace("Pause the job: " + jobKey);
			}
			return setJobState(jobKey, JobState.PAUSED);
		}
		return getJobRuntime(jobKey).getJobState();
	}

	@Override
	public JobState resumeJob(JobKey jobKey) throws SQLException {
		if (hasJob(jobKey) && hasJobState(jobKey, JobState.PAUSED)) {
			if (log.isTraceEnabled()) {
				log.trace("Pause the job: " + jobKey);
			}
			return setJobState(jobKey, JobState.SCHEDULING);
		}
		return getJobRuntime(jobKey).getJobState();
	}

	@Override
	public int getJobId(JobKey jobKey) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Integer result = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_ID,
					new Object[] { jobKey.getClusterName(), jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() },
					Integer.class);
			if (result != null) {
				return result.intValue();
			}
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
		return 0;
	}

	@Override
	public int persistJob(JobDefinition jobDef, String attachment) throws SQLException {
		final JobKey jobKey = JobKey.of(jobDef);

		int jobId;
		TriggerBuilder triggerBuilder;
		TriggerType triggerType;
		if (hasJob(jobKey)) {
			jobId = getJobId(jobKey);
			Connection connection = null;
			try {
				connection = dataSource.getConnection();
				connection.setAutoCommit(false);
				JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_DETAIL, new Object[] { jobDef.getDescription(), attachment,
						jobDef.getEmail(), jobDef.getRetries(), jobDef.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });

				triggerBuilder = jobDef.buildTrigger();
				triggerType = triggerBuilder.getTriggerType();
				JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_TRIGGER,
						new Object[] { triggerBuilder.getTriggerType().getValue(),
								JacksonUtils.toJsonString(triggerBuilder.getTriggerDescription()),
								triggerBuilder.getStartDate() != null ? new Timestamp(triggerBuilder.getStartDate().getTime()) : null,
								triggerBuilder.getEndDate() != null ? new Timestamp(triggerBuilder.getEndDate().getTime()) : null, jobId });

				connection.commit();
				log.info("Merge job info '{}' ok.", jobKey);
			} catch (SQLException e) {
				JdbcUtils.rollbackQuietly(connection);
				throw e;
			} finally {
				JdbcUtils.closeQuietly(connection);
			}

			if (triggerType == TriggerType.SERIAL) {
				JobKey[] dependencies = triggerBuilder.getTriggerDescription().getSerial().getDependencies();
				handleJobDependency(jobKey, jobId, dependencies);
			}
			jobListenerContainer.signalAll(jobKey, JobAction.REFRESH);
			return jobId;
		} else {
			Connection connection = null;
			try {
				connection = dataSource.getConnection();
				connection.setAutoCommit(false);
				jobId = JdbcUtils.insert(connection, SqlScripts.DEF_INSERT_JOB_DETAIL, ps -> {
					ps.setString(1, jobDef.getClusterName());
					ps.setString(2, jobDef.getGroupName());
					ps.setString(3, jobDef.getJobName());
					ps.setString(4, jobDef.getJobClassName());
					ps.setString(5, jobDef.getDescription());
					ps.setString(6, attachment);
					ps.setString(7, jobDef.getEmail());
					ps.setInt(8, jobDef.getRetries());
					ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
				});

				JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_RUNTIME, ps -> {
					ps.setInt(1, jobId);
					ps.setInt(2, JobState.NOT_SCHEDULED.getValue());
				});

				triggerBuilder = jobDef.buildTrigger();
				triggerType = triggerBuilder.getTriggerType();
				JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_TRIGGER, ps -> {
					ps.setInt(1, jobId);
					ps.setInt(2, triggerType.getValue());
					ps.setString(3, JacksonUtils.toJsonString(triggerBuilder.getTriggerDescription()));
					ps.setTimestamp(4,
							triggerBuilder.getStartDate() != null ? new Timestamp(triggerBuilder.getStartDate().getTime()) : null);
					ps.setTimestamp(5, triggerBuilder.getEndDate() != null ? new Timestamp(triggerBuilder.getEndDate().getTime()) : null);
				});
				connection.commit();
				log.info("Add job info '{}' ok.", jobKey);

			} catch (SQLException e) {
				JdbcUtils.rollbackQuietly(connection);
				throw e;
			} finally {
				JdbcUtils.closeQuietly(connection);
			}

			if (triggerType == TriggerType.SERIAL) {
				JobKey[] dependencies = triggerBuilder.getTriggerDescription().getSerial().getDependencies();
				handleJobDependency(jobKey, jobId, dependencies);
			}

			jobListenerContainer.signalAll(jobKey, JobAction.CREATION);
			return jobId;
		}
	}

	private void handleJobDependency(JobKey jobKey, int jobId, JobKey[] dependencies) throws SQLException {
		List<Integer> dependentIds = new ArrayList<Integer>();
		if (ArrayUtils.isNotEmpty(dependencies)) {
			for (JobKey dependency : dependencies) {
				if (hasJob(dependency)) {
					dependentIds.add(getJobId(dependency));
				} else {
					jobListenerContainer.addObserver(dependency.getIndentifier(), new FutureDependentJobUpdater(jobKey, dependency));
					if (log.isTraceEnabled()) {
						log.trace("Dependent job '{}' is not available now and will be triggered in the future.", dependency);
					}
				}
			}
		}
		if (dependentIds.size() > 0) {
			Connection connection = null;
			try {
				connection = dataSource.getConnection();
				connection.setAutoCommit(false);
				JdbcUtils.update(connection, SqlScripts.DEF_DELETE_JOB_DEPENDENCY, new Object[] { jobId });
				if (dependentIds.size() > 0) {
					JdbcUtils.batchUpdate(connection, SqlScripts.DEF_INSERT_JOB_DEPENDENCY, ps -> {
						for (Integer dependentId : dependentIds) {
							ps.setInt(1, jobId);
							ps.setInt(2, dependentId);
							ps.addBatch();
						}
					});
				}
				connection.commit();
				log.info("Add job dependency by key '{}' ok.", jobKey);
			} catch (SQLException e) {
				JdbcUtils.rollbackQuietly(connection);
				throw e;
			} finally {
				JdbcUtils.closeQuietly(connection);
			}
		}
	}

	/**
	 * 
	 * FutureDependentJobUpdater
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	private class FutureDependentJobUpdater implements Observer {

		private final JobKey jobKey;
		private final JobKey dependency;

		FutureDependentJobUpdater(JobKey jobKey, JobKey dependency) {
			this.jobKey = jobKey;
			this.dependency = dependency;
		}

		@Override
		public void update(Observable ob, Object arg) {
			Set<JobKey> jobKeys = new TreeSet<JobKey>();
			jobKeys.add(dependency);
			JobTriggerDetail triggerDetail = null;
			try {
				triggerDetail = getJobTriggerDetail(jobKey);
				Serial serial = triggerDetail.getTriggerDescription().getSerial();
				JobKey[] dependencies = serial.getDependencies();
				if (ArrayUtils.isNotEmpty(dependencies)) {
					jobKeys.addAll(Arrays.asList(dependencies));
				}
				serial.setDependencies(jobKeys.toArray(new JobKey[0]));
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			}
			if (triggerDetail != null) {
				Connection connection = null;
				try {
					final int jobId = getJobId(jobKey);
					final int dependentId = getJobId(dependency);
					connection = dataSource.getConnection();
					connection.setAutoCommit(false);
					JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_TRIGGER, new Object[] {
							triggerDetail.getTriggerType().getValue(), JacksonUtils.toJsonString(triggerDetail.getTriggerDescription()),
							triggerDetail.getStartDate() != null ? new Timestamp(triggerDetail.getStartDate().getTime()) : null,
							triggerDetail.getEndDate() != null ? new Timestamp(triggerDetail.getEndDate().getTime()) : null, jobId });

					JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_DEPENDENCY, ps -> {
						ps.setInt(1, jobId);
						ps.setInt(2, dependentId);
					});
					connection.commit();
					log.info("Add job dependency '{}' to jobId {} ok.", dependency, jobId);
				} catch (SQLException e) {
					JdbcUtils.rollbackQuietly(connection);
					log.error(e.getMessage(), e);
				} finally {
					JdbcUtils.closeQuietly(connection);
				}
			}
		}

	}

	@Override
	public JobState deleteJob(JobKey jobKey) throws SQLException {
		if (!hasJob(jobKey)) {
			throw new JobBeanNotFoundException(jobKey);
		}
		if (!hasJobState(jobKey, JobState.NOT_SCHEDULED)) {
			throw new JobException("Please unschedule the job before you delete it.");
		}
		try {
			return setJobState(jobKey, JobState.FINISHED);
		} finally {
			jobListenerContainer.signalAll(jobKey, JobAction.DELETION);
		}
	}

	@Override
	public boolean hasJob(JobKey jobKey) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Integer result = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_NAME_EXISTS,
					new Object[] { jobKey.getClusterName(), jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() },
					Integer.class);
			return result != null && result.intValue() > 0;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobState setJobState(JobKey jobKey, JobState jobState) throws SQLException {
		final int jobId = getJobId(jobKey);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_STATE, new Object[] { jobState.getValue(), jobId });
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
		return getJobRuntime(jobKey).getJobState();
	}

	@Override
	public boolean hasJobState(JobKey jobKey, JobState jobState) throws SQLException {
		JobRuntime jobRuntime = getJobRuntime(jobKey);
		return jobRuntime.getJobState() == jobState;
	}

	@Override
	public JobDetail getJobDetail(JobKey jobKey) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_DETAIL,
					new Object[] { jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });
			return tuple.toBean(JobDetail.class);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobTriggerDetail getJobTriggerDetail(JobKey jobKey) throws SQLException {
		final int jobId = getJobId(jobKey);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_TRIGGER, new Object[] { jobId });
			return tuple.toBean(JobTriggerDetail.class);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public boolean hasRelations(JobKey jobKey) throws SQLException {
		final int jobId = getJobId(jobKey);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Integer rowCount = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_HAS_RELATION, new Object[] { jobId },
					Integer.class);
			return rowCount != null && rowCount.intValue() > 0;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobKey[] getDependencies(JobKey jobKey) throws SQLException {
		List<JobKey> jobKeys = new ArrayList<JobKey>();
		final int jobId = getJobId(jobKey);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			List<Tuple> dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_JOB_DEPENDENCIES, new Object[] { jobId });
			for (Tuple tuple : dataList) {
				jobKeys.add(tuple.toBean(JobKey.class));
			}
			return jobKeys.toArray(new JobKey[0]);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobRuntime getJobRuntime(JobKey jobKey) throws SQLException {
		final int jobId = getJobId(jobKey);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_RUNTIME, new Object[] { jobId });
			return tuple != null ? tuple.toBean(JobRuntime.class) : null;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobKey[] getJobKeys(JobQuery jobQuery) throws SQLException {
		List<JobKey> jobKeys = new ArrayList<JobKey>();
		Connection connection = null;
		List<Tuple> dataList = null;
		try {
			connection = dataSource.getConnection();
			dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_JOB_KEYS,
					new Object[] { jobQuery.getClusterName(), jobQuery.getTriggerType().getValue() });
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
		if (CollectionUtils.isNotEmpty(dataList)) {
			for (Tuple tuple : dataList) {
				jobKeys.add(tuple.toBean(JobKey.class));
			}
		}
		return jobKeys.toArray(new JobKey[0]);
	}

	@Override
	public JobStat getJobStat(JobKey jobKey) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_STAT,
					new Object[] { jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });
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
			public int rowCount() {
				return delegate.rowCount();
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
			public int rowCount() {
				return delegate.rowCount();
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

	/**
	 * 
	 * NewJobCreationListener
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	class NewJobCreationListener implements JobListener {

		@Override
		public void afterCreation(JobKey jobKey) {
			if (log.isTraceEnabled()) {
				log.trace("Create new Job: {}", jobKey);
			}
		}

	}

}
