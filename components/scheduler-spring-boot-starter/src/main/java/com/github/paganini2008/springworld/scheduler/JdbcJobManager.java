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
	public void pauseJob(JobKey jobKey) throws SQLException {
		if (hasJob(jobKey) && hasJobState(jobKey, JobState.SCHEDULING)) {
			setJobState(jobKey, JobState.PAUSED);
			if (log.isTraceEnabled()) {
				log.trace("Pause the job: " + jobKey);
			}
		}
	}

	@Override
	public void resumeJob(JobKey jobKey) throws SQLException {
		if (hasJob(jobKey) && hasJobState(jobKey, JobState.PAUSED)) {
			setJobState(jobKey, JobState.SCHEDULING);
			if (log.isTraceEnabled()) {
				log.trace("Pause the job: " + jobKey);
			}
		}
	}

	@Override
	public int addJob(Job job, String attachment) throws SQLException {
		JobKey jobKey = JobKey.of(job);
		if (hasJob(jobKey)) {
			return 0;
		}
		int id;
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			id = JdbcUtils.insert(connection, SqlScripts.DEF_INSERT_JOB_DETAIL, ps -> {
				ps.setString(1, jobKey.getIdentifier());
				ps.setString(2, job.getGroupName());
				ps.setString(3, job.getJobName());
				ps.setString(4, job.getJobClassName());
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
				Trigger trigger = job.getTrigger();
				ps.setInt(1, id);
				ps.setInt(2, trigger.getTriggerType().getValue());
				ps.setString(3, JacksonUtils.toJsonString(trigger.getTriggerDescription()));
			});

			connection.commit();
			log.info("Add job '" + jobKey + "' ok.");
			return id;
		} catch (SQLException e) {
			JdbcUtils.rollbackQuietly(connection);
			throw e;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}

	}

	@Override
	public void deleteJob(JobKey jobKey) throws SQLException {
		if (!hasJob(jobKey)) {
			throw new JobBeanNotFoundException(jobKey.toString());
		}
		if (!hasJobState(jobKey, JobState.NOT_SCHEDULED)) {
			throw new JobException("Please unschedule the job before you delete it.");
		}
		setJobState(jobKey, JobState.FINISHED);
	}

	@Override
	public boolean hasJob(JobKey jobKey) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Integer result = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_NAME_EXISTS,
					new Object[] { jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() }, Integer.class);
			return result != null && result.intValue() > 0;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public void setJobState(JobKey jobKey, JobState jobState) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_STATE,
					new Object[] { jobState.getValue(), jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
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
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_TRIGGER,
					new Object[] { jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });
			return tuple.toBean(JobTriggerDetail.class);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public JobRuntime getJobRuntime(JobKey jobKey) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_RUNTIME,
					new Object[] { jobKey.getGroupName(), jobKey.getJobName(), jobKey.getJobClassName() });
			return tuple.toBean(JobRuntime.class);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
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

}
