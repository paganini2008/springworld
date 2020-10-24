package com.github.paganini2008.springworld.jobstorm.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.ConnectionFactory;
import com.github.paganini2008.devtools.jdbc.DefaultPageableSql;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.devtools.jdbc.PageResponse;
import com.github.paganini2008.devtools.jdbc.PageableQuery;
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

	@Autowired
	private ConnectionFactory connectionFactory;

	@Autowired
	private ScheduleManager scheduleManager;

	@Autowired
	private JobBeanLoader jobBeanLoader;

	public void initializeJobBeans() throws Exception {
		PageableQuery<Tuple> query = JdbcUtils.pageableQuery(connectionFactory,
				new DefaultPageableSql(SqlScripts.DEF_SELECT_ALL_AVAILABLE_JOB_DETAIL), new Object[0]);
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
