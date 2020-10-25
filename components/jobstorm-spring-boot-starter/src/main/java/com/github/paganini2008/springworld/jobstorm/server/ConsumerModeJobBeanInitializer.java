package com.github.paganini2008.springworld.jobstorm.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.jobstorm.BeanNames;
import com.github.paganini2008.springworld.jobstorm.DependencyType;
import com.github.paganini2008.springworld.jobstorm.Job;
import com.github.paganini2008.springworld.jobstorm.JobBeanLoader;
import com.github.paganini2008.springworld.jobstorm.JobFutureHolder;
import com.github.paganini2008.springworld.jobstorm.JobKey;
import com.github.paganini2008.springworld.jobstorm.JobManager;
import com.github.paganini2008.springworld.jobstorm.JobParallelizationListener;
import com.github.paganini2008.springworld.jobstorm.JobRuntimeListenerContainer;
import com.github.paganini2008.springworld.jobstorm.JobBeanInitializer;
import com.github.paganini2008.springworld.jobstorm.SerialDependencyScheduler;
import com.github.paganini2008.springworld.jobstorm.TriggerType;
import com.github.paganini2008.springworld.jobstorm.model.JobKeyQuery;

/**
 * 
 * ConsumerModeJobBeanInitializer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsumerModeJobBeanInitializer implements JobBeanInitializer {

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
	private SerialDependencyScheduler serialDependencyScheduler;

	@Autowired
	private JobFutureHolder jobFutureHolder;

	@Autowired
	private JobRuntimeListenerContainer jobRuntimeListenerContainer;

	public void initializeJobBeans() throws Exception {
		initializeJobDependencyBeans();
	}

	private void initializeJobDependencyBeans() throws Exception {
		JobKeyQuery jobQuery = new JobKeyQuery();
		jobQuery.setClusterName(clusterName);
		jobQuery.setTriggerType(TriggerType.DEPENDENT);
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

				// update or schedule serial dependency job
				dependencies = jobManager.getDependencies(jobKey, DependencyType.SERIAL);
				if (ArrayUtils.isNotEmpty(dependencies)) {
					if (serialDependencyScheduler.hasScheduled(jobKey)) {
						serialDependencyScheduler.updateDependency(job, dependencies);
					} else {
						jobFutureHolder.add(jobKey, serialDependencyScheduler.scheduleDependency(job, dependencies));
					}
				}

				// add listener to watch parallel dependency job done
				dependencies = jobManager.getDependencies(jobKey, DependencyType.PARALLEL);
				if (ArrayUtils.isNotEmpty(dependencies)) {
					for (JobKey dependency : dependencies) {
						jobRuntimeListenerContainer.addListener(dependency, ApplicationContextUtils.instantiateClass(JobParallelizationListener.class));
					}
				}
			}
		}
	}

}
