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

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * EmbeddedJobStore
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class EmbeddedJobStore implements JobStore {

	private static final Map<String, String> sqlMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		{
			put("cluster_job_detail", SqlScripts.DEF_DDL_JOB_DETAIL);
			put("cluster_job_runtime", SqlScripts.DEF_DDL_JOB_RUNTIME);
			put("cluster_job_trace", SqlScripts.DEF_DDL_JOB_TRACE);
			put("cluster_job_exception", SqlScripts.DEF_DDL_JOB_EXCEPTION);
		}
	};

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JobLoadingMode jobLoadingMode;

	@Autowired
	private ScheduleManager scheduleManager;

	@Override
	public void configure() throws SQLException {
		Connection connection = null;
		for (Map.Entry<String, String> entry : new HashMap<String, String>(sqlMap).entrySet()) {
			try {
				connection = dataSource.getConnection();
				if (!JdbcUtils.existsTable(connection, null, entry.getKey())) {
					JdbcUtils.update(connection, entry.getValue());
					log.info("Create table: " + entry.getKey());
				}
			} finally {
				JdbcUtils.closeQuietly(connection);
			}
		}
	}

	@Override
	public void reloadJobs() throws SQLException {
		Connection connection = null;
		List<Tuple> dataList = null;
		try {
			connection = dataSource.getConnection();
			dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_ALL_JOB_DETAIL);
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
		if (CollectionUtils.isNotEmpty(dataList)) {
			for (Tuple tuple : dataList) {
				Job job = jobLoadingMode.defineJob(GenericJobDefinition.load(tuple));
				scheduleManager.schedule(job, tuple.get("attachment"));
			}
		}
		log.info("Reload and schedule all customized jobs ok.");
	}

	protected void loadJob(Tuple tuple, JobLoadingMode callback) {
		final String jobClassName = (String) tuple.get("jobClassName");
		Class<?> jobClass = ClassUtils.forName(jobClassName);
		if (!Job.class.isAssignableFrom(jobClass)) {
			throw new JobException("Class '" + jobClass.getName() + "' is not a implementor of Job interface.");
		}

		if (ApplicationContextUtils.getBean(jobClass) != null) {
			return;
		}
		Job job = (Job) BeanUtils.instantiate(jobClass);
		callback.postLoad(job, tuple.get("attachment"));
		log.info("Reload job '" + job.getSignature() + "' from database successfully.");
	}

	@Override
	public void addJob(Job job) throws SQLException {
		checkJobSignature(job);
		if (hasJob(job)) {
			return;
		}

		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			int id = JdbcUtils.insert(connection, SqlScripts.DEF_INSERT_JOB_DETAIL, ps -> {
				ps.setString(1, job.getJobName());
				ps.setString(2, job.getJobClassName());
				ps.setString(3, job.getGroupName());
				ps.setInt(4, JobType.valueOf(job).getValue());
				ps.setString(5, job.getDescription());
				ps.setString(6, job.getAttachment());
				ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
			});

			JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_RUNTIME, ps -> {
				ps.setInt(1, id);
				ps.setInt(2, JobState.NOT_SCHEDULED.getValue());
			});
			connection.commit();
			log.info("Add job '" + job.getSignature() + "' ok.");
		} catch (SQLException e) {
			JdbcUtils.rollbackQuietly(connection);
			throw e;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public void deleteJob(Job job) throws SQLException {
		checkJobSignature(job);
		if (hasJob(job)) {
			Connection connection = null;
			try {
				connection = dataSource.getConnection();
				JdbcUtils.update(connection, SqlScripts.DEF_DELETE_JOB_DETAIL, new Object[] { job.getJobName(), job.getJobClassName() });
				log.info("Delete job '" + job.getSignature() + "' ok.");
			} finally {
				JdbcUtils.closeQuietly(connection);
			}
		}
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
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_RUNTIME,
					new Object[] { jobState.getValue(), job.getJobName(), job.getJobClassName() });
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

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
	public JobRuntime getJobRuntime(Job job) throws Exception {
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
	public JobStat getJobStat(Job job) throws Exception {
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
		final ResultSetSlice<Tuple> delegate = JdbcUtils.pageableQuery(dataSource, SqlScripts.DEF_SELECT_ALL_JOB_INFO, (Object[]) null);
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
	public ResultSetSlice<JobStat> getJobStat(StatType statType) throws Exception {
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
