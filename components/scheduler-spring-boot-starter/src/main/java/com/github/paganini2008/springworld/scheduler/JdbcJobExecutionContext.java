package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.io.SerializationUtils;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;

/**
 * 
 * JdbcJobExecutionContext
 *
 * @author Fred Feng
 * @since 1.0
 */
public class JdbcJobExecutionContext implements JobExecutionContext {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private BroadcastVar broadcastVar;

	@Override
	public JobRuntime getJobRuntime(Job job) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_PANEL_DETAIL, new Object[] { job.getJobName() });
			return tuple.toBean(SimpleJobRuntime.class);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public void writeVar(String name, Object value) {
		broadcastVar.writeVar(name, value);
	}

	@Override
	public Object readVar(String name) {
		return broadcastVar.readVar(name);
	}

	@Override
	public void pause(Job job) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_STATE, new Object[] { JobState.PAUSED.getValue(), job.getJobName() });
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public void resume(Job job) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			JdbcUtils.update(connection, SqlScripts.DEF_UPDATE_JOB_STATE, new Object[] { JobState.RUNNING.getValue(), job.getJobName() });
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public Object getAttachment(Job job) throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Tuple tuple = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_JOB_DETAIL, new Object[] { job.getJobName() });
			byte[] data = (byte[]) tuple.get("attachment");
			if (data != null && data.length > 0) {
				return SerializationUtils.deserialize(data, false);
			}
			return null;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

}
