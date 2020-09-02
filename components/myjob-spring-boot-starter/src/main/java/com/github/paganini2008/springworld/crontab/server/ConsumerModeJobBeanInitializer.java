package com.github.paganini2008.springworld.crontab.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springworld.crontab.BeanNames;
import com.github.paganini2008.springworld.crontab.Job;
import com.github.paganini2008.springworld.crontab.JobBeanLoader;
import com.github.paganini2008.springworld.crontab.JobDependencyFuture;
import com.github.paganini2008.springworld.crontab.JobDependencyObservable;
import com.github.paganini2008.springworld.crontab.JobFutureHolder;
import com.github.paganini2008.springworld.crontab.JobKey;
import com.github.paganini2008.springworld.crontab.JobManager;
import com.github.paganini2008.springworld.crontab.NotManagedJobBeanInitializer;
import com.github.paganini2008.springworld.crontab.TriggerType;
import com.github.paganini2008.springworld.crontab.model.JobQuery;

/**
 * 
 * ConsumerModeJobBeanInitializer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsumerModeJobBeanInitializer implements NotManagedJobBeanInitializer {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private JobManager jobManager;

	@Qualifier(BeanNames.INTERNAL_JOB_BEAN_LOADER)
	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Qualifier(BeanNames.EXTERNAL_JOB_BEAN_LOADER)
	@Autowired(required = false)
	private JobBeanLoader externalJobBeanLoader;

	@Autowired
	private JobDependencyObservable jobDependencyObservable;

	@Autowired
	private JobFutureHolder jobFutureHolder;

	public void initizlizeJobBeans() throws Exception {
		JobQuery jobQuery = new JobQuery();
		jobQuery.setClusterName(clusterName);
		jobQuery.setTriggerType(TriggerType.SERIAL);
		JobKey[] jobKeys = jobManager.getJobKeys(jobQuery);
		if (ArrayUtils.isNotEmpty(jobKeys)) {
			Job job;
			JobKey[] dependencies;
			for (JobKey jobKey : jobKeys) {
				job = jobBeanLoader.loadJobBean(jobKey);
				if (job == null && externalJobBeanLoader != null) {
					job = externalJobBeanLoader.loadJobBean(jobKey);
				}
				if (job == null) {
					continue;
				}
				dependencies = jobManager.getDependencies(jobKey);
				if (ArrayUtils.isNotEmpty(dependencies) && !(jobFutureHolder.get(jobKey) instanceof JobDependencyFuture)) {
					jobFutureHolder.add(jobKey, jobDependencyObservable.addDependency(job, dependencies));
				}
			}
		}
	}

}
