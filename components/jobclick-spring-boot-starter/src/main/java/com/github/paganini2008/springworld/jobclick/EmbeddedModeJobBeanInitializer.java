package com.github.paganini2008.springworld.jobclick;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * EmbeddedModeJobBeanInitializer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class EmbeddedModeJobBeanInitializer implements NotManagedJobBeanInitializer {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ScheduleManager scheduleManager;

	@Qualifier(BeanNames.INTERNAL_JOB_BEAN_LOADER)
	@Autowired
	private JobBeanLoader internalJobBeanLoader;

	@Qualifier(BeanNames.EXTERNAL_JOB_BEAN_LOADER)
	@Autowired(required = false)
	private JobBeanLoader externalJobBeanLoader;

	public void initizlizeJobBeans() throws Exception {
		refreshInternalJobBeans();
		if (externalJobBeanLoader != null) {
			refreshExternalJobBeans();
		}
	}

	private void refreshInternalJobBeans() throws Exception {
		Connection connection = null;
		List<Tuple> dataList = null;
		try {
			connection = dataSource.getConnection();
			dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_JOB_DETAIL_BY_GROUP_NAME, new Object[] { applicationName });
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
				try {
					job = internalJobBeanLoader.loadJobBean(jobKey);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					continue;
				}
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

	private void refreshExternalJobBeans() throws Exception {
		Connection connection = null;
		List<Tuple> dataList = null;
		try {
			connection = dataSource.getConnection();
			dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_JOB_DETAIL_BY_OTHER_GROUP_NAME,
					new Object[] { applicationName });
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
				try {
					job = externalJobBeanLoader.loadJobBean(jobKey);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					continue;
				}
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
