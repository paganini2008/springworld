package com.github.paganini2008.springworld.cronkeeper;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.jdbc.PageRequest;
import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springworld.cronkeeper.model.JobDetail;
import com.github.paganini2008.springworld.cronkeeper.model.JobLog;
import com.github.paganini2008.springworld.cronkeeper.model.JobQuery;
import com.github.paganini2008.springworld.cronkeeper.model.JobRuntime;
import com.github.paganini2008.springworld.cronkeeper.model.JobStackTrace;
import com.github.paganini2008.springworld.cronkeeper.model.JobTrace;
import com.github.paganini2008.springworld.cronkeeper.model.JobTracePageQuery;
import com.github.paganini2008.springworld.cronkeeper.model.JobTraceQuery;
import com.github.paganini2008.springworld.cronkeeper.model.JobTriggerDetail;
import com.github.paganini2008.springworld.cronkeeper.model.PageQuery;

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
			put("ck_server_detail", SqlScripts.DEF_DDL_CLUSTER_DETAIL);
			put("ck_job_detail", SqlScripts.DEF_DDL_JOB_DETAIL);
			put("ck_job_trigger", SqlScripts.DEF_DDL_JOB_TRIGGER);
			put("ck_job_runtime", SqlScripts.DEF_DDL_JOB_RUNTIME);
			put("ck_job_trace", SqlScripts.DEF_DDL_JOB_TRACE);
			put("ck_job_exception", SqlScripts.DEF_DDL_JOB_EXCEPTION);
			put("ck_job_log", SqlScripts.DEF_DDL_JOB_LOG);
			put("ck_job_dependency", SqlScripts.DEF_DDL_JOB_DEPENDENCY);
		}
	};

	@Qualifier(BeanNames.DATA_SOURCE)
	@Autowired
	private DataSource dataSource;

	@Value("${spring.application.cluster.scheduler.createTable:true}")
	private boolean createTable;

	@Autowired
	private LifecycleListenerContainer lifecycleListenerContainer;

	@Autowired
	private JobIdCache jobIdCache;

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
	}

	@Override
	public String[] selectClusterNames() throws SQLException {
		List<String> clusterNames = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			List<Tuple> list = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_CLUSTER_NAME);
			for (Tuple tuple : list) {
				clusterNames.add(tuple.getProperty("clusterName"));
			}
			return clusterNames.toArray(new String[0]);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
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
	public int getJobId(final JobKey jobKey) throws SQLException {
		return jobIdCache.getJobId(jobKey, () -> {
			return doGetJobId(jobKey);
		});
	}

	private int doGetJobId(JobKey jobKey) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Integer result = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_ID,
					new Object[] { jobKey.getClusterName(), jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() },
					Integer.class);
			if (result != null) {
				return result.intValue();
			} else {
				throw new JobBeanNotFoundException(jobKey);
			}
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
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
				JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_DETAIL,
						new Object[] { jobDef.getDescription(), attachment, jobDef.getEmail(), jobDef.getRetries(), jobDef.getClusterName(),
								jobDef.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });

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
			lifecycleListenerContainer.signalAll(jobKey, JobAction.REFRESH);
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

			lifecycleListenerContainer.signalAll(jobKey, JobAction.CREATION);
			return jobId;
		}
	}

	private void handleJobDependency(JobKey jobKey, int jobId, JobKey[] dependencies) throws SQLException {
		List<Integer> dependentIds = new ArrayList<Integer>();
		if (ArrayUtils.isNotEmpty(dependencies)) {
			for (JobKey dependency : dependencies) {
				if (hasJob(dependency)) {
					dependentIds.add(getJobId(dependency));
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
			jobIdCache.evict(jobKey);
			lifecycleListenerContainer.signalAll(jobKey, JobAction.DELETION);
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
	public JobDetail getJobDetail(JobKey jobKey, boolean detailed) throws SQLException {
		JobDetail jobDetail = doGetJobDetail(jobKey);
		if (detailed) {
			jobDetail.setJobRuntime(getJobRuntime(jobKey));
			jobDetail.setJobTriggerDetail(getJobTriggerDetail(jobKey));
		}
		return jobDetail;
	}

	private JobDetail doGetJobDetail(JobKey jobKey) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_DETAIL,
					new Object[] { jobKey.getClusterName(), jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });
			if (tuple == null) {
				throw new JobBeanNotFoundException(jobKey);
			}
			JobDetail jobDetail = tuple.toBean(JobDetail.class);
			jobDetail.setJobKey(JobKey.by(tuple.getProperty("clusterName"), tuple.getProperty("groupName"), tuple.getProperty("jobName"),
					tuple.getProperty("jobClassName")));
			return jobDetail;
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
			if (tuple == null) {
				throw new JobBeanNotFoundException(jobKey);
			}
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
		Set<JobKey> jobKeys = new TreeSet<JobKey>();
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
			if (tuple == null) {
				throw new JobBeanNotFoundException(jobKey);
			}
			return tuple.toBean(JobRuntime.class);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobKey[] getJobKeys(JobQuery jobQuery) throws SQLException {
		Set<JobKey> jobKeys = new TreeSet<JobKey>();
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
	public void selectJobDetail(PageQuery<JobDetail> pageQuery) {
		final ResultSetSlice<Tuple> delegate = JdbcUtils.pageableQuery(dataSource, SqlScripts.DEF_SELECT_JOB_INFO,
				new Object[] { pageQuery.getClusterName() });
		ResultSetSlice<JobDetail> resultSetSlice = new ResultSetSlice<JobDetail>() {

			@Override
			public int rowCount() {
				return delegate.rowCount();
			}

			@Override
			public List<JobDetail> list(int maxResults, int firstResult) {
				List<JobDetail> dataList = new ArrayList<JobDetail>(maxResults);
				for (Tuple tuple : delegate.list(maxResults, firstResult)) {
					JobDetail jobDetail = tuple.toBean(JobDetail.class);
					JobKey jobKey = tuple.toBean(JobKey.class);
					JobRuntime jobRuntime = tuple.toBean(JobRuntime.class);
					JobTriggerDetail jobTriggerDetail = tuple.toBean(JobTriggerDetail.class);
					jobDetail.setJobKey(jobKey);
					jobDetail.setJobRuntime(jobRuntime);
					jobDetail.setJobTriggerDetail(jobTriggerDetail);
					dataList.add(jobDetail);
				}
				return dataList;
			}

		};

		PageResponse<JobDetail> pageResponse = resultSetSlice.list(PageRequest.of(pageQuery.getPage(), pageQuery.getSize()));
		int rows = pageResponse.getTotalRecords();
		pageQuery.setRows(rows);
		pageQuery.setContent(pageResponse.getContent());
		pageQuery.setNextPage(pageResponse.hasNextPage());
	}

	@Override
	public void selectJobTrace(JobTracePageQuery<JobTrace> pageQuery) throws Exception {
		Date startDate = pageQuery.getStartDate();
		if (startDate == null) {
			startDate = DateUtils.addDays(new Date(), -30);
			startDate = DateUtils.setTime(startDate, 0, 0, 0);
		}
		Date endDate = pageQuery.getEndDate();
		if (endDate == null) {
			endDate = DateUtils.setTime(new Date(), 23, 59, 59);
		}
		final int jobId = getJobId(pageQuery.getJobKey());
		final ResultSetSlice<Tuple> delegate = JdbcUtils.pageableQuery(dataSource, SqlScripts.DEF_SELECT_JOB_TRACE,
				new Object[] { jobId, startDate, endDate });
		ResultSetSlice<JobTrace> resultSetSlice = new ResultSetSlice<JobTrace>() {

			@Override
			public int rowCount() {
				return delegate.rowCount();
			}

			@Override
			public List<JobTrace> list(int maxResults, int firstResult) {
				List<JobTrace> dataList = new ArrayList<JobTrace>(maxResults);
				for (Tuple tuple : delegate.list(maxResults, firstResult)) {
					JobTrace jobTrace = tuple.toBean(JobTrace.class);
					dataList.add(jobTrace);
				}
				return dataList;
			}

		};
		PageResponse<JobTrace> pageResponse = resultSetSlice.list(PageRequest.of(pageQuery.getPage(), pageQuery.getSize()));
		int rows = pageResponse.getTotalRecords();
		pageQuery.setRows(rows);
		pageQuery.setContent(pageResponse.getContent());
		pageQuery.setNextPage(pageResponse.hasNextPage());
	}

	@Override
	public JobStackTrace[] selectJobStackTrace(JobTraceQuery query) throws SQLException {
		List<JobStackTrace> data = new ArrayList<JobStackTrace>();
		final int jobId = getJobId(query.getJobKey());
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			List<Tuple> dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_JOB_EXCEPTION,
					new Object[] { jobId, query.getTraceId() });
			for (Tuple tuple : dataList) {
				data.add(tuple.toBean(JobStackTrace.class));
			}
			return data.toArray(new JobStackTrace[0]);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobLog[] selectJobLog(JobTraceQuery query) throws SQLException {
		List<JobLog> data = new ArrayList<JobLog>();
		final int jobId = getJobId(query.getJobKey());
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			List<Tuple> dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_JOB_LOG,
					new Object[] { jobId, query.getTraceId() });
			for (Tuple tuple : dataList) {
				data.add(tuple.toBean(JobLog.class));
			}
			return data.toArray(new JobLog[0]);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}

}
