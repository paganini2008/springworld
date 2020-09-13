package com.github.paganini2008.springworld.cronfall.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springworld.cronfall.BeanNames;
import com.github.paganini2008.springworld.cronfall.Job;
import com.github.paganini2008.springworld.cronfall.JobBeanLoader;
import com.github.paganini2008.springworld.cronfall.JobDependencyFuture;
import com.github.paganini2008.springworld.cronfall.JobDependencyObservable;
import com.github.paganini2008.springworld.cronfall.JobFutureHolder;
import com.github.paganini2008.springworld.cronfall.JobKey;
import com.github.paganini2008.springworld.cronfall.JobManager;
import com.github.paganini2008.springworld.cronfall.NotManagedJobBeanInitializer;
import com.github.paganini2008.springworld.cronfall.TriggerType;
import com.github.paganini2008.springworld.cronfall.model.JobQuery;

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
