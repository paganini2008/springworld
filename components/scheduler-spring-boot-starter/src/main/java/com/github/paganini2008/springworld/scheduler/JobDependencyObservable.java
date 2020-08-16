package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.Observer;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.redisplus.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobDependencyObservable
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class JobDependencyObservable extends Observable {

	public JobDependencyObservable() {
		super(true);
	}

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Qualifier("main-job-executor")
	@Autowired
	private JobExecutor jobExecutor;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	public JobFuture addDependency(final Job job, final String[] dependencies) {
		List<Observer> obs = new ArrayList<Observer>();
		for (String dependency : dependencies) {
			Observer ob = (o, attachment) -> {
				jobExecutor.execute(job, attachment);
			};
			addObserver(dependency, ob);
			obs.add(ob);
		}
		return new JobDependencyFuture(obs.toArray(new Observer[0]), dependencies, this);
	}

	@Slf4j
	public static class JobDependencyFuture implements JobFuture {

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
			DataSource dataSource = ApplicationContextUtils.getBean("scheduler-ds", DataSource.class);
			Connection connection = null;
			try {
				connection = dataSource.getConnection();
				Date latestDate = JdbcUtils.fetchOne(connection, SqlScripts.DEF_SELECT_LATEST_EXECUTION_TIME_OF_JOB_DEPENDENCIES,
						new Object[] { ArrayUtils.join(dependencies, ",") }, Date.class);
				return latestDate != null ? latestDate.getTime() : -1L;
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
				return -1L;
			} finally {
				JdbcUtils.closeQuietly(connection);
			}
		}

	}

	public void executeDependency(JobKey jobKey, Object attachment) {
		notifyObservers(jobKey.getIdentifier(), attachment);
	}

	public void notifyDependencies(JobKey jobKey, Object result) {
		final String channel = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:dependency:"
				+ jobKey.getIdentifier();
		JobParam jobParam = new JobParam(jobKey, result);
		redisMessageSender.sendMessage(channel, jobParam);
	}

}
