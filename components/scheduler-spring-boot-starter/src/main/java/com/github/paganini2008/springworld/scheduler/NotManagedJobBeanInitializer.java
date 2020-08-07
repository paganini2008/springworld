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
 * NotManagedJobBeanInitializer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class NotManagedJobBeanInitializer implements JobBeanInitializer {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired
	private JobBeanLoader jobBeanLoader;

	public void loadJobs() throws Exception {
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
			JobParameter jobParameter;
			Job job;
			for (Tuple tuple : dataList) {
				jobParameter = tuple.toBean(JobParameter.class);
				job = jobBeanLoader.defineJob(jobParameter);
				if (job == null || job.managedByApplicationContext()) {
					continue;
				}
				if (scheduleManager.hasScheduled(job)) {
					continue;
				}
				scheduleManager.schedule(job, tuple.get("attachment"));
				log.info("Reload and schedule Job '{}' ok.", job.getSignature());
			}
		}
	}

}
