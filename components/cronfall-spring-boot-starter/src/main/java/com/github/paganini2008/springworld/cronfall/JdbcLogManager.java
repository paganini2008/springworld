package com.github.paganini2008.springworld.cronfall;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JdbcLogManager
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JdbcLogManager implements LogManager {

	@Qualifier(BeanNames.DATA_SOURCE)
	@Autowired
	private DataSource dataSource;

	@Autowired
	private JobManager jobManager;

	@Override
	public void log(long traceId, JobKey jobKey, LogLevel logLevel, String messagePattern, Object[] args, String[] stackTraces) {
		final int jobId = getJobId(jobKey);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			FormattingTuple tuple = MessageFormatter.arrayFormat(messagePattern, args);
			JdbcUtils.update(connection, SqlScripts.DEF_INSERT_JOB_LOG,
					new Object[] { traceId, jobId, logLevel.name(), tuple.getMessage(), new Timestamp(System.currentTimeMillis()) });
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
		if (ArrayUtils.isNotEmpty(stackTraces)) {
			List<Object[]> argsList = new ArrayList<Object[]>();
			for (String stackTrace : stackTraces) {
				argsList.add(new Object[] { traceId, jobId, stackTrace });
			}
			try {
				connection = dataSource.getConnection();
				JdbcUtils.batchUpdate(connection, SqlScripts.DEF_INSERT_JOB_EXCEPTION, argsList);
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			} finally {
				JdbcUtils.closeQuietly(connection);
			}
		}
	}

	@Override
	public void error(long traceId, JobKey jobKey, String[] stackTraces) {
		Assert.isNull(stackTraces, "Null throwable");
		final int jobId = getJobId(jobKey);
		List<Object[]> argsList = new ArrayList<Object[]>();
		for (String stackTrace : stackTraces) {
			argsList.add(new Object[] { traceId, jobId, stackTrace });
		}
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			JdbcUtils.batchUpdate(connection, SqlScripts.DEF_INSERT_JOB_EXCEPTION, argsList);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	private int getJobId(JobKey jobKey) {
		try {
			return jobManager.getJobId(jobKey);
		} catch (Exception e) {
			throw com.github.paganini2008.springworld.cronfall.ExceptionUtils.wrapExeception(e);
		}
	}

}
