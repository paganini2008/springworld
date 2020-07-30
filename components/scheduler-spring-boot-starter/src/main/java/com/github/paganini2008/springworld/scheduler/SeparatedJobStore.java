package com.github.paganini2008.springworld.scheduler;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;

/**
 * 
 * SeparatedJobStore
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class SeparatedJobStore extends EmbeddedJobStore {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private DataSource dataSource;

	@Override
	protected void loadJob(Tuple tuple, JobLoadingCallback callback) {
		Job job = new JobConfig(tuple);
		int jobId = (Integer) tuple.get("jobId");
		String json = getTriggerDescription(jobId);
		Map<String, Object> data;
		try {
			data = objectMapper.readValue(json, HashMap.class);
		} catch (IOException e) {
			throw new IllegalStateException(e);
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

	private static class CronJobConfig implements InvocationHandler {

		private final Job instance;
		private final Job cronJob;
		private final Map<String, Object> config;

		CronJobConfig(Job instance) {
			this.instance = instance;
			this.cronJob = (CronJob) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { CronJob.class }, this);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			if ("getCronExcpetion".equals(methodName)) {
				return config.get("cron");
			} else {
				return method.invoke(instance, args);
			}
		}

	}

}
