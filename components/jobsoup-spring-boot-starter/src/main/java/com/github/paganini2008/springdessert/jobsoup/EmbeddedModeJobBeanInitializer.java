package com.github.paganini2008.springdessert.jobsoup;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.ConnectionFactory;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;
import com.github.paganini2008.springdessert.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springdessert.jobsoup.model.JobKeyQuery;

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
public class EmbeddedModeJobBeanInitializer implements JobBeanInitializer {

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private ConnectionFactory connectionFactory;

	@Autowired
	private ScheduleManager scheduleManager;

	@Qualifier(BeanNames.INTERNAL_JOB_BEAN_LOADER)
	@Autowired
	private JobBeanLoader internalJobBeanLoader;

	@Qualifier(BeanNames.EXTERNAL_JOB_BEAN_LOADER)
	@Autowired(required = false)
	private JobBeanLoader externalJobBeanLoader;

	@Autowired
	private JobRuntimeListenerContainer jobRuntimeListenerContainer;

	public void initializeJobBeans() throws Exception {
		refreshInternalJobBeans();
		if (externalJobBeanLoader != null) {
			refreshExternalJobBeans();
		}

		initializeJobDependencyBeans();
	}

	private void refreshInternalJobBeans() throws Exception {
		Connection connection = null;
		List<Tuple> dataList = null;
		try {
			connection = connectionFactory.getConnection();
			dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_JOB_DETAIL_BY_GROUP_NAME,
					new Object[] { clusterName, applicationName });
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

	private void refreshExternalJobBeans() throws Exception {
		Connection connection = null;
		List<Tuple> dataList = null;
		try {
			connection = connectionFactory.getConnection();
			dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_JOB_DETAIL_BY_OTHER_GROUP_NAME,
					new Object[] { clusterName, applicationName });
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

	private void initializeJobDependencyBeans() throws Exception {
		JobKeyQuery jobQuery = new JobKeyQuery();
		jobQuery.setClusterName(clusterName);
		jobQuery.setTriggerType(TriggerType.DEPENDENT);
		JobKey[] jobKeys = jobManager.getJobKeys(jobQuery);
		if (ArrayUtils.isNotEmpty(jobKeys)) {
			JobKey[] dependencies;
			for (JobKey jobKey : jobKeys) {
				// add listener to watch parallel dependency job done
				dependencies = jobManager.getDependentKeys(jobKey, DependencyType.PARALLEL);
				if (ArrayUtils.isNotEmpty(dependencies)) {
					for (JobKey dependency : dependencies) {
						jobRuntimeListenerContainer.addListener(dependency,
								ApplicationContextUtils.instantiateClass(JobParallelizationListener.class));
					}
				}
			}
		}
	}

}
