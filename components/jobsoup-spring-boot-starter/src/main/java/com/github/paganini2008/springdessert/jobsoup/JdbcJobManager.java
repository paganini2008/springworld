package com.github.paganini2008.springdessert.jobsoup;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.devtools.jdbc.ConnectionFactory;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.jdbc.PageRequest;
import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springdessert.jobsoup.model.JobDetail;
import com.github.paganini2008.springdessert.jobsoup.model.JobKeyQuery;
import com.github.paganini2008.springdessert.jobsoup.model.JobLog;
import com.github.paganini2008.springdessert.jobsoup.model.JobRuntime;
import com.github.paganini2008.springdessert.jobsoup.model.JobStackTrace;
import com.github.paganini2008.springdessert.jobsoup.model.JobTrace;
import com.github.paganini2008.springdessert.jobsoup.model.JobTracePageQuery;
import com.github.paganini2008.springdessert.jobsoup.model.JobTraceQuery;
import com.github.paganini2008.springdessert.jobsoup.model.JobTriggerDetail;
import com.github.paganini2008.springdessert.jobsoup.model.PageQuery;
import com.github.paganini2008.springdessert.jobsoup.model.TriggerDescription;
import com.github.paganini2008.springdessert.jobsoup.model.TriggerDescription.Dependency;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JdbcJobManager
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class JdbcJobManager implements JobManager, BeanPostProcessor {

	@Autowired
	private ConnectionFactory connectionFactory;

	@Autowired
	private LifeCycleListenerContainer lifeCycleListenerContainer;

	@Autowired
	private JobIdCache jobIdCache;

	@Override
	public String[] selectClusterNames() throws SQLException {
		List<String> clusterNames = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
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
			connection = connectionFactory.getConnection();
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
		Trigger trigger;
		TriggerType triggerType;
		DependencyType dependencyType = null;
		boolean hasDependentKeys = ArrayUtils.isNotEmpty(jobDef.getDependentKeys());
		boolean hasSubKeys = ArrayUtils.isNotEmpty(jobDef.getSubKeys());
		if (hasDependentKeys && hasSubKeys) {
			dependencyType = DependencyType.MIXED;
		} else if (hasDependentKeys && !hasSubKeys) {
			dependencyType = DependencyType.SERIAL;
		} else if (!hasDependentKeys && hasSubKeys) {
			dependencyType = DependencyType.PARALLEL;
		}

		if (hasJob(jobKey)) {
			jobId = getJobId(jobKey);
			Connection connection = null;
			try {
				connection = connectionFactory.getConnection();
				connection.setAutoCommit(false);
				JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_DETAIL,
						new Object[] { jobDef.getDescription(), attachment, jobDef.getEmail(), jobDef.getRetries(), jobDef.getWeight(),
								jobDef.getTimeout(), jobDef.getClusterName(), jobDef.getGroupName(), jobKey.getJobName(),
								jobKey.getJobClassName() });

				trigger = jobDef.getTrigger();
				triggerType = trigger.getTriggerType();

				if (dependencyType != null) {
					TriggerDescription triggerDescription = trigger.getTriggerDescription();
					triggerDescription.setDependency(new Dependency());
					Dependency dependency = triggerDescription.getDependency();
					dependency.setDependencyType(dependencyType);
					switch (dependencyType) {
					case SERIAL:
						dependency.setDependentKeys(jobDef.getDependentKeys());
						triggerDescription.setCron(null);
						triggerDescription.setPeriodic(null);
						break;
					case PARALLEL:
						dependency.setSubKeys(jobDef.getSubKeys());
						dependency.setCompletionRate(jobDef.getCompletionRate());
						dependency.setTriggerType(triggerType);
						if (triggerType == TriggerType.CRON) {
							triggerDescription.getDependency().setCron(triggerDescription.getCron());
							triggerDescription.setCron(null);
						} else if (triggerType == TriggerType.PERIODIC) {
							triggerDescription.getDependency().setPeriodic(triggerDescription.getPeriodic());
							triggerDescription.setPeriodic(null);
						}
						break;
					case MIXED:
						dependency.setDependentKeys(jobDef.getDependentKeys());
						dependency.setSubKeys(jobDef.getSubKeys());
						triggerDescription.setCron(null);
						triggerDescription.setPeriodic(null);
						break;
					}

				}

				JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_TRIGGER,
						new Object[] { hasDependentKeys || hasSubKeys ? TriggerType.DEPENDENT.getValue() : triggerType.getValue(),
								JacksonUtils.toJsonString(trigger.getTriggerDescription()),
								trigger.getStartDate() != null ? new Timestamp(trigger.getStartDate().getTime()) : null,
								trigger.getEndDate() != null ? new Timestamp(trigger.getEndDate().getTime()) : null,
								trigger.getRepeatCount(), jobId });

				connection.commit();
				log.info("Merge job '{}' ok.", jobKey);
			} catch (SQLException e) {
				JdbcUtils.rollbackQuietly(connection);
				throw e;
			} finally {
				JdbcUtils.closeQuietly(connection);
			}

			if (dependencyType != null) {
				switch (dependencyType) {
				case SERIAL:
					handleJobDependency(jobKey, jobId, jobDef.getDependentKeys(), DependencyType.SERIAL);
					break;
				case PARALLEL:
					handleJobDependency(jobKey, jobId, jobDef.getSubKeys(), DependencyType.PARALLEL);
					break;
				case MIXED:
					handleJobDependency(jobKey, jobId, jobDef.getDependentKeys(), DependencyType.SERIAL);
					handleJobDependency(jobKey, jobId, jobDef.getSubKeys(), DependencyType.PARALLEL);
					break;
				}

			}

			lifeCycleListenerContainer.onChange(jobKey, JobLifeCycle.REFRESH);
			return jobId;
		} else {
			Connection connection = null;
			try {
				connection = connectionFactory.getConnection();
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
					ps.setInt(9, jobDef.getWeight());
					ps.setLong(10, jobDef.getTimeout());
					ps.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
				});

				JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_RUNTIME, ps -> {
					ps.setInt(1, jobId);
					ps.setInt(2, JobState.NOT_SCHEDULED.getValue());
				});

				trigger = jobDef.getTrigger();
				triggerType = trigger.getTriggerType();

				if (dependencyType != null) {
					TriggerDescription triggerDescription = trigger.getTriggerDescription();
					triggerDescription.setDependency(new Dependency());
					Dependency dependency = triggerDescription.getDependency();
					dependency.setDependencyType(dependencyType);
					switch (dependencyType) {
					case SERIAL:
						dependency.setDependentKeys(jobDef.getDependentKeys());
						triggerDescription.setCron(null);
						triggerDescription.setPeriodic(null);
						break;
					case PARALLEL:
						dependency.setSubKeys(jobDef.getSubKeys());
						dependency.setCompletionRate(jobDef.getCompletionRate());
						dependency.setTriggerType(triggerType);
						if (triggerType == TriggerType.CRON) {
							triggerDescription.getDependency().setCron(triggerDescription.getCron());
							triggerDescription.setCron(null);
						} else if (triggerType == TriggerType.PERIODIC) {
							triggerDescription.getDependency().setPeriodic(triggerDescription.getPeriodic());
							triggerDescription.setPeriodic(null);
						}
						break;
					case MIXED:
						dependency.setDependentKeys(jobDef.getDependentKeys());
						dependency.setSubKeys(jobDef.getSubKeys());
						triggerDescription.setCron(null);
						triggerDescription.setPeriodic(null);
						break;
					}

				}

				JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_TRIGGER, ps -> {
					ps.setInt(1, jobId);
					ps.setInt(2, hasDependentKeys || hasSubKeys ? TriggerType.DEPENDENT.getValue() : triggerType.getValue());
					ps.setString(3, JacksonUtils.toJsonString(trigger.getTriggerDescription()));
					ps.setTimestamp(4, trigger.getStartDate() != null ? new Timestamp(trigger.getStartDate().getTime()) : null);
					ps.setTimestamp(5, trigger.getEndDate() != null ? new Timestamp(trigger.getEndDate().getTime()) : null);
					ps.setInt(6, trigger.getRepeatCount());
				});
				connection.commit();
				log.info("Add job '{}' ok.", jobKey);

			} catch (SQLException e) {
				JdbcUtils.rollbackQuietly(connection);
				throw e;
			} finally {
				JdbcUtils.closeQuietly(connection);
			}

			if (dependencyType != null) {
				switch (dependencyType) {
				case SERIAL:
					handleJobDependency(jobKey, jobId, jobDef.getDependentKeys(), DependencyType.SERIAL);
					break;
				case PARALLEL:
					handleJobDependency(jobKey, jobId, jobDef.getSubKeys(), DependencyType.PARALLEL);
					break;
				case MIXED:
					handleJobDependency(jobKey, jobId, jobDef.getDependentKeys(), DependencyType.SERIAL);
					handleJobDependency(jobKey, jobId, jobDef.getSubKeys(), DependencyType.PARALLEL);
					break;
				}

			}

			lifeCycleListenerContainer.onChange(jobKey, JobLifeCycle.CREATION);
			return jobId;
		}
	}

	/**
	 * Save Job Dependency Info
	 * 
	 * @param jobKey
	 * @param jobId
	 * @param dependencies
	 * @param dependencyType
	 * @throws SQLException
	 */
	private void handleJobDependency(JobKey jobKey, int jobId, JobKey[] dependencies, DependencyType dependencyType) throws SQLException {
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
				connection = connectionFactory.getConnection();
				connection.setAutoCommit(false);
				JdbcUtils.update(connection, SqlScripts.DEF_DELETE_JOB_DEPENDENCY, new Object[] { jobId, dependencyType.getValue() });
				if (dependentIds.size() > 0) {
					JdbcUtils.batchUpdate(connection, SqlScripts.DEF_INSERT_JOB_DEPENDENCY, ps -> {
						for (Integer dependentId : dependentIds) {
							ps.setInt(1, jobId);
							ps.setInt(2, dependentId);
							ps.setInt(3, dependencyType.getValue());
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
	public JobState finishJob(JobKey jobKey) throws SQLException {
		if (!hasJob(jobKey)) {
			throw new JobBeanNotFoundException(jobKey);
		}
		if (!hasJobState(jobKey, JobState.NOT_SCHEDULED)) {
			throw new JobException("Please unschedule the job before you delete it.");
		}
		try {
			lifeCycleListenerContainer.onChange(jobKey, JobLifeCycle.DELETION);
			return setJobState(jobKey, JobState.FINISHED);
		} finally {
			jobIdCache.evict(jobKey);
		}
	}

	@Override
	public boolean hasJob(JobKey jobKey) throws SQLException {
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
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
			connection = connectionFactory.getConnection();
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
			connection = connectionFactory.getConnection();
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
			connection = connectionFactory.getConnection();
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
	public boolean hasRelations(JobKey jobKey, DependencyType dependencyType) throws SQLException {
		final int jobId = getJobId(jobKey);
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
			Integer rowCount = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_HAS_RELATION,
					new Object[] { jobId, dependencyType.getValue() }, Integer.class);
			return rowCount != null && rowCount.intValue() > 0;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobKey[] getRelations(JobKey jobKey, DependencyType dependencyType) throws Exception {
		Set<JobKey> jobKeys = new TreeSet<JobKey>();
		final int jobId = getJobId(jobKey);
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
			List<Tuple> dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_JOB_RELATIONS,
					new Object[] { jobId, dependencyType.getValue() });
			for (Tuple tuple : dataList) {
				jobKeys.add(tuple.toBean(JobKey.class));
			}
			return jobKeys.toArray(new JobKey[0]);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobKey[] getDependentKeys(JobKey jobKey, DependencyType dependencyType) throws SQLException {
		final int jobId = getJobId(jobKey);
		Set<JobKey> jobKeys = new TreeSet<JobKey>();
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
			List<Tuple> dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_DEPENDENT_JOB_KEYS,
					new Object[] { jobId, dependencyType.getValue() });
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
			connection = connectionFactory.getConnection();
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
	public JobKey[] getJobKeys(JobKeyQuery jobQuery) throws SQLException {
		Set<JobKey> jobKeys = new TreeSet<JobKey>();
		Connection connection = null;
		List<Tuple> dataList = null;
		try {
			connection = connectionFactory.getConnection();
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
		final ResultSetSlice<Tuple> delegate = JdbcUtils.pageableQuery(connectionFactory, SqlScripts.DEF_SELECT_JOB_INFO,
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
		final ResultSetSlice<Tuple> delegate = JdbcUtils.pageableQuery(connectionFactory, SqlScripts.DEF_SELECT_JOB_TRACE,
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
			connection = connectionFactory.getConnection();
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
			connection = connectionFactory.getConnection();
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

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Job) {
			Job job = (Job) bean;
			try {
				persistJob(job, null);
			} catch (Exception e) {
				throw new BeanInitializationException(e.getMessage(), e);
			}
		}
		return bean;
	}

}
