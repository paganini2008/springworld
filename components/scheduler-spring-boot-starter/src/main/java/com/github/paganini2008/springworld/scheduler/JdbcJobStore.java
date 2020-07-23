package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
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
import com.github.paganini2008.devtools.io.SerializationUtils;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.jdbc.ResultSetSlice;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JdbcJobStore
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JdbcJobStore implements JobStore {

	private static final Map<String, String> sqlMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		{
			put("my_job_detail", SqlScripts.DEF_DDL_JOB_DETAIL);
			put("my_job_panel", SqlScripts.DEF_DDL_JOB_PANEL);
			put("my_job_cron_trigger", SqlScripts.DEF_DDL_JOB_CRON_TRIGGER);
			put("my_job_periodic_trigger", SqlScripts.DEF_DDL_JOB_PERIODIC_TRIGGER);
			put("my_job_dependency", SqlScripts.DEF_DDL_JOB_DEPENDENCY);
		}
	};

	@Autowired
	private DataSource dataSource;

	@Override
	public void initialize() throws SQLException {
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
	public void loadExistedJobs(JobLoadingCallback callback) throws SQLException {
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
		if (CollectionUtils.isNotCollection(dataList)) {
			for (Tuple tuple : dataList) {
				loadJob(tuple, callback);
			}
		}
		log.info("Reload and schedule all customized jobs ok.");
	}

	private void loadJob(Tuple tuple, JobLoadingCallback callback) {
		final String jobName = (String) tuple.get("jobName");
		final String jobClassName = (String) tuple.get("jobClassName");

		Class<?> jobClass = ClassUtils.forName(jobClassName);
		if (!Job.class.isAssignableFrom(jobClass)) {
			throw new JobException("Class '" + jobClass.getName() + "' is not a implementor of Job interface.");
		}

		if (ApplicationContextUtils.getBean(jobClass) != null) {
			return;
		}
		Job job = (Job) BeanUtils.instantiate(jobClass);
		if (callback != null) {
			callback.afterLoad(job);
		}
		log.info("Reload job '" + jobName + "' from database ok.");
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
			JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_DETAIL, ps -> {
				ps.setString(1, job.getJobName());
				ps.setString(2, job.getJobClassName());
				ps.setString(3, job.getDescription());
				byte[] bytes = new byte[0];
				if (job.getAttachment() != null) {
					bytes = SerializationUtils.serialize(job.getAttachment(), false);
				}
				ps.setBytes(4, bytes);
				ps.setLong(5, System.currentTimeMillis());
			});

			JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_PANEL, ps -> {
				ps.setString(1, job.getJobName());
				ps.setInt(2, JobState.NOT_SCHEDULED.getValue());
			});
			connection.commit();
			log.info("Add job '" + job.getJobName() + "' ok.");
		} catch (SQLException e) {
			JdbcUtils.rollbackQuietly(connection);
			throw e;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public void saveJobDepentency(SerializableJob job) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			List<Object[]> argsList = new ArrayList<Object[]>();
			for (String jobName : job.getDependencies()) {
				argsList.add(new Object[] { job.getJobName(), jobName });
			}
			JdbcUtils.batchUpdate(connection, SqlScripts.DEF_INSERT_JOB_DEPENDENCY, argsList);
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
				JdbcUtils.update(connection, SqlScripts.DEF_DELETE_JOB_DETAIL, new Object[] { job.getJobName() });
				log.info("Delete job '" + job.getJobName() + "' ok.");
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
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_STATE, new Object[] { jobState.getValue(), job.getJobName() });
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}
	
	@Override
	public ResultSetSlice<JobInfo> getJobInfos() {
		final ResultSetSlice<Tuple> delegate = JdbcUtils.pageableQuery(dataSource, SqlScripts.DEF_SELECT_ALL_JOB_DETAIL, (Object[]) null);
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

	static void checkJobSignature(Job job) {
		if (StringUtils.isBlank(job.getJobName()) || StringUtils.isBlank(job.getJobClassName())) {
			throw new JobException("Invalid job signature: " + job.getSignature());
		}
	}

}
