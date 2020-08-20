package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.Observer;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobDependencyFuture
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JobDependencyFuture implements JobFuture {

	private final Observer[] observers;
	private final String[] dependencies;
	private final Observable observable;
	private final AtomicBoolean cancelled = new AtomicBoolean();
	private final AtomicBoolean done = new AtomicBoolean();

	JobDependencyFuture(Observer[] observers, String[] dependencies, Observable observable) {
		this.observers = observers;
		this.dependencies = dependencies;
		this.observable = observable;
	}

	@Override
	public void cancel() {
		for (int i = 0; i < dependencies.length; i++) {
			observable.deleteObserver(dependencies[i], observers[i]);
		}
		cancelled.set(true);
		done.set(true);
	}

	@Override
	public boolean isDone() {
		return done.get();
	}

	@Override
	public boolean isCancelled() {
		return cancelled.get();
	}

	@Override
	public long getNextExectionTime(Date lastExecutionTime, Date lastActualExecutionTime, Date lastCompletionTime) {
		DataSource dataSource = ApplicationContextUtils.getBean(BeanNames.DATA_SOURCE, DataSource.class);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Date latestDate = JdbcUtils.fetchOne(connection, String.format(SqlScripts.DEF_SELECT_LATEST_EXECUTION_TIME_OF_JOB_DEPENDENCIES,
					StringUtils.join(dependencies, "'", "'", ",")), Date.class);
			return latestDate != null ? latestDate.getTime() : -1L;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			return -1L;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

}
