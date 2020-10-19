package com.github.paganini2008.springworld.jobswarm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.Observer;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SerialDependencyFuture
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class SerialDependencyFuture implements JobFuture {

	private final List<JobKey> dependencies;
	private final List<Observer> observers;
	private final Observable observable;
	private final AtomicBoolean cancelled = new AtomicBoolean();
	private final AtomicBoolean done = new AtomicBoolean();

	SerialDependencyFuture(List<JobKey> dependencies, List<Observer> observers, Observable observable) {
		this.dependencies = dependencies;
		this.observers = observers;
		this.observable = observable;
	}

	@Override
	public void cancel() {
		for (int i = 0; i < dependencies.size(); i++) {
			observable.deleteObserver(dependencies.get(i).getIdentifier(), observers.get(i));
		}
		cancelled.set(true);
		done.set(false);
	}

	@Override
	public boolean isDone() {
		return done.get();
	}

	@Override
	public boolean isCancelled() {
		return cancelled.get();
	}

	public List<JobKey> getDependencies() {
		return dependencies;
	}

	public List<Observer> getObservers() {
		return observers;
	}

	@Override
	public long getNextExectionTime(Date lastExecutionTime, Date lastActualExecutionTime, Date lastCompletionTime) {
		JobManager jobManager = ApplicationContextUtils.getBean(JobManager.class);
		List<Integer> jobIds = new ArrayList<Integer>();
		try {
			for (JobKey jobKey : dependencies) {
				if (jobManager.hasJob(jobKey)) {
					jobIds.add(jobManager.getJobId(jobKey));
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return NEXT_EXECUTION_TIME_NOT_FOUND;
		}
		if (jobIds.isEmpty()) {
			return NEXT_EXECUTION_TIME_NOT_FOUND;
		}
		DataSource dataSource = ApplicationContextUtils.getBean(BeanNames.DATA_SOURCE, DataSource.class);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Date latestDate = JdbcUtils.fetchOne(connection,
					String.format(SqlScripts.DEF_SELECT_LATEST_EXECUTION_TIME_OF_DEPENDENT_JOBS, CollectionUtils.join(jobIds, ",")),
					Date.class);
			return latestDate != null ? latestDate.getTime() : NEXT_EXECUTION_TIME_NOT_FOUND;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			return NEXT_EXECUTION_TIME_NOT_FOUND;
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

}
