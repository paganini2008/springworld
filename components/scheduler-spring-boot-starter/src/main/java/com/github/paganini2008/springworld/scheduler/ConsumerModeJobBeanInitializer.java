package com.github.paganini2008.springworld.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.Tuple;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;

/**
 * 
 * ConsumerModeJobBeanInitializer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsumerModeJobBeanInitializer implements NotManagedJobBeanInitializer {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JobManager jobManager;

	@Qualifier("internal-job-bean-loader")
	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Qualifier("external-job-bean-loader")
	@Autowired(required = false)
	private JobBeanLoader externalJobBeanLoader;

	@Autowired
	private JobDependencyObservable jobDependencyObservable;

	@Autowired
	private JobFutureHolder jobFutureHolder;

	public void initizlizeJobBeans() throws Exception {
		Connection connection = null;
		List<Tuple> dataList = null;
		try {
			connection = dataSource.getConnection();
			dataList = JdbcUtils.fetchAll(connection, SqlScripts.DEF_SELECT_ALL_DEPENDENT_JOB_DETAIL);
		} catch (SQLException e) {
			throw new JobException(e.getMessage(), e);
		} finally {
			JdbcUtils.closeQuietly(connection);
		}
		if (CollectionUtils.isNotEmpty(dataList)) {
			JobKey jobKey;
			Job job;
			JobTriggerDetail triggerDetail;
			for (Tuple tuple : dataList) {
				jobKey = tuple.toBean(JobKey.class);
				job = jobBeanLoader.loadJobBean(jobKey);
				if (job == null && externalJobBeanLoader != null) {
					job = externalJobBeanLoader.loadJobBean(jobKey);
				}
				if (job == null) {
					continue;
				}
				triggerDetail = jobManager.getJobTriggerDetail(jobKey);
				String[] dependencies = triggerDetail.getTriggerDescription().getDependencies();
				if (ArrayUtils.isNotEmpty(dependencies) && !(jobFutureHolder.get(jobKey) instanceof JobDependencyFuture)) {
					jobFutureHolder.add(jobKey, jobDependencyObservable.addDependency(job, dependencies));
				}
			}
		}
	}

}
