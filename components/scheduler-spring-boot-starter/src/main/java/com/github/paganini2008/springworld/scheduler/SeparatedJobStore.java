package com.github.paganini2008.springworld.scheduler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.devtools.reflection.MethodUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SeparatedJobStore
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class SeparatedJobStore extends EmbeddedJobStore {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private DataSource dataSource;

	@Override
	protected void loadJob(Tuple tuple, JobLoadingMode callback) {
		Job job = new JobConfig(tuple);
		int jobId = (Integer) tuple.get("jobId");
		String json = getTriggerDescription(jobId);
		Map<String, Object> config;
		try {
			config = objectMapper.readValue(json, HashMap.class);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		JobType jobType = JobType.valueOf((Integer) tuple.get("jobType"));
		callback.postLoad(job, tuple.get("attachment"));
	}

	private String getTriggerDescription(int jobId) {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_TRIGGER, new Object[] { jobId });
			return tuple.getProperty("json");
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	/**
	 * 
	 * SerializableJobAspect
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	private static class SerializableJobAspect implements Aspect {

		private final Map<String, Object> data;

		SerializableJobAspect(Map<String, Object> data) {
			this.data = data;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			if ("getDependencies".equals(methodName)) {
				return data.get("dependencies");
			}
			return MethodUtils.invokeMethod(target, method, args);
		}

		@Override
		public void catchException(Object target, Method method, Object[] args, Throwable e) {
			log.info(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * CronJobAspect
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	private static class CronJobAspect implements Aspect {

		private final Map<String, Object> data;

		CronJobAspect(Map<String, Object> data) {
			this.data = data;
		}

		@Override
		public Object call(Object target, Method method, Object[] args) {
			final String methodName = method.getName();
			if ("getCronExpression".equals(methodName)) {
				return data.get("cron");
			}
			return MethodUtils.invokeMethod(target, method, args);
		}

		@Override
		public void catchException(Object target, Method method, Object[] args, Throwable e) {
			log.info(e.getMessage(), e);
		}

	}

}
