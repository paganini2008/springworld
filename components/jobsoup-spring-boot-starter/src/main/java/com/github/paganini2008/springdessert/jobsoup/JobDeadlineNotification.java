package com.github.paganini2008.springdessert.jobsoup;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.ConnectionFactory;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springworld.cluster.utils.BeanLifeCycle;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobDeadlineNotification
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JobDeadlineNotification implements JobRuntimeListener, Executable, BeanLifeCycle {

	private final Map<JobKey, Date> deadlines = new ConcurrentHashMap<JobKey, Date>();
	private Timer timer;

	@Autowired
	private ConnectionFactory connectionFactory;

	@Override
	public void configure() throws Exception {
		refresh();
		this.timer = ThreadUtils.scheduleAtFixedRate(this, 1, TimeUnit.MINUTES);
	}

	@Override
	public void beforeRun(long traceId, JobKey jobKey, Object attachment, Date startDate) {
		Date theDeadline = deadlines.get(jobKey);
		if (theDeadline != null && theDeadline.before(startDate)) {
			deadlines.remove(jobKey);
			throw new JobTerminationException(jobKey, "Job '" + jobKey + "' has terminated on deadline: " + theDeadline);
		}
	}

	@Override
	public boolean execute() {
		refresh();
		return true;
	}

	private void refresh() {
		Connection connection = null;
		try {
			connection = connectionFactory.getConnection();
			List<Tuple> dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_ALL_JOB_TRIGGER_DEADLINE, new Object[0]);
			JobKey jobKey;
			Date endDate;
			for (Tuple tuple : dataList) {
				jobKey = tuple.toBean(JobKey.class);
				endDate = (Date) tuple.get("endDate");
				deadlines.put(jobKey, endDate);
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@Override
	public void destroy() {
		if (timer != null) {
			timer.cancel();
		}
	}

}
