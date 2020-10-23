package com.github.paganini2008.springworld.jobstorm.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.DefaultPageableSql;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.devtools.jdbc.PageableQuery;
import com.github.paganini2008.springworld.jobstorm.BeanNames;
import com.github.paganini2008.springworld.jobstorm.Job;
import com.github.paganini2008.springworld.jobstorm.JobBeanLoader;
import com.github.paganini2008.springworld.jobstorm.JobKey;
import com.github.paganini2008.springworld.jobstorm.NotManagedJobBeanInitializer;
import com.github.paganini2008.springworld.jobstorm.ScheduleManager;
import com.github.paganini2008.springworld.jobstorm.SqlScripts;

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

	@Qualifier(BeanNames.DATA_SOURCE)
	@Autowired
	private DataSource dataSource;

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired
	private JobBeanLoader jobBeanLoader;

	public void initializeJobBeans() throws Exception {
		Connection connection = null;
		PageableQuery<Tuple> query = null;
		try {
			connection = dataSource.getConnection();
			query = JdbcUtils.pageableQuery(dataSource, new DefaultPageableSql(SqlScripts.DEF_SELECT_ALL_JOB_DETAIL), new Object[0]);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
		if (query != null) {
			List<Tuple> dataList;
			JobKey jobKey;
			Job job;
			for (PageResponse<Tuple> pageResponse : query.forEach(1, 10)) {
				dataList = pageResponse.getContent();
				for (Tuple tuple : dataList) {
					jobKey = tuple.toBean(JobKey.class);
					try {
						job = jobBeanLoader.loadJobBean(jobKey);
					} catch (Exception e) {
						log.error("Unable to load job Bean: {}", jobKey, e);
						continue;
					}
					if (job == null) {
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

}
