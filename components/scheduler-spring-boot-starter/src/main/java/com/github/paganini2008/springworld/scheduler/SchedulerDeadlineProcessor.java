package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SchedulerDeadlineProcessor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class SchedulerDeadlineProcessor implements JobListener, Executable, Lifecycle {

	private final Map<JobKey, Date> deadlines = new ConcurrentHashMap<JobKey, Date>();
	private Timer timer;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	@Override
	public void configure() throws Exception {
		refresh();
		timer = ThreadUtils.scheduleAtFixedRate(this, 1, TimeUnit.MINUTES);
	}

	@Override
	public void beforeRun(JobKey jobKey, Date startDate) {
		Date theDeadline = deadlines.get(jobKey);
		if (theDeadline != null && theDeadline.before(startDate)) {
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
			connection = dataSource.getConnection();
			List<Tuple> dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_ALL_JOB_TRIGGER_DEADLINE, new Object[0]);
			String identifier;
			Date endDate;
			for (Tuple tuple : dataList) {
				identifier = (String) tuple.get("identifier");
				endDate = (Date) tuple.get("endDate");
				deadlines.put(JobKey.of(identifier), endDate);
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
	}

	@PreDestroy
	@Override
	public void close() {
		if (timer != null) {
			timer.cancel();
		}
	}

}
