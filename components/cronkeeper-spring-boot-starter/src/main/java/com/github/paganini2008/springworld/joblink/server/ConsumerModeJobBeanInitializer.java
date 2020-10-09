package com.github.paganini2008.springworld.joblink.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springworld.joblink.BeanNames;
import com.github.paganini2008.springworld.joblink.Job;
import com.github.paganini2008.springworld.joblink.JobBeanLoader;
import com.github.paganini2008.springworld.joblink.JobDependencyFuture;
import com.github.paganini2008.springworld.joblink.JobDependencyObservable;
import com.github.paganini2008.springworld.joblink.JobFutureHolder;
import com.github.paganini2008.springworld.joblink.JobKey;
import com.github.paganini2008.springworld.joblink.JobManager;
import com.github.paganini2008.springworld.joblink.NotManagedJobBeanInitializer;
import com.github.paganini2008.springworld.joblink.TriggerType;
import com.github.paganini2008.springworld.joblink.model.JobKeyQuery;

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
		JobKeyQuery jobQuery = new JobKeyQuery();
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
