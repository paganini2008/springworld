package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ProducerModeJobBeanInitializer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ProducerModeJobBeanInitializer implements NotManagedJobBeanInitializer {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired
	private JobBeanLoader jobBeanLoader;

	public void initizlizeJobBeans() throws Exception {
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
		if (CollectionUtils.isNotEmpty(dataList)) {
			JobKey jobKey;
			Job job;
			for (Tuple tuple : dataList) {
				jobKey = tuple.toBean(JobKey.class);
				job = jobBeanLoader.loadJobBean(jobKey);
				if (job == null || job.managedByApplicationContext()) {
					continue;
				}
				if (scheduleManager.hasScheduled(jobKey)) {
					continue;
				}
				scheduleManager.schedule(job);
				log.info("Reload and schedule Job '{}' ok.", jobKey);
			}
		}
	}

}
