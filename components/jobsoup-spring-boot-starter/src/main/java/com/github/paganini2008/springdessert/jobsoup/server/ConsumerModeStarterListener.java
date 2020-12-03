package com.github.paganini2008.springdessert.jobsoup.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterRefreshedEvent;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.InstanceId;
import com.github.paganini2008.springdessert.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springdessert.jobsoup.DependencyType;
import com.github.paganini2008.springdessert.jobsoup.JobKey;
import com.github.paganini2008.springdessert.jobsoup.JobManager;
import com.github.paganini2008.springdessert.jobsoup.JobParallelizationListener;
import com.github.paganini2008.springdessert.jobsoup.JobRuntimeListenerContainer;
import com.github.paganini2008.springdessert.jobsoup.TriggerType;
import com.github.paganini2008.springdessert.jobsoup.model.JobKeyQuery;
import com.github.paganini2008.springdessert.jobsoup.model.JobResult;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsumerModeStarterListener
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class ConsumerModeStarterListener implements ApplicationListener<ApplicationClusterRefreshedEvent> {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private ConsumerModeRestTemplate restTemplate;

	@Autowired
	private JobRuntimeListenerContainer jobRuntimeListenerContainer;

	@Override
	public void onApplicationEvent(ApplicationClusterRefreshedEvent event) {
		registerCluster();

		handleParallelDependencies();
	}

	private void registerCluster() {
		final ApplicationInfo applicationInfo = instanceId.getApplicationInfo();
		ResponseEntity<JobResult<Boolean>> responseEntity = restTemplate.perform(null, "/job/admin/registerCluster", HttpMethod.POST,
				applicationInfo, new ParameterizedTypeReference<JobResult<Boolean>>() {
				});
		if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody().getData()) {
			log.info("'{}' register to job producer ok.", applicationInfo);
		}
	}

	private void handleParallelDependencies() {
		JobKeyQuery jobQuery = new JobKeyQuery();
		jobQuery.setClusterName(clusterName);
		jobQuery.setTriggerType(TriggerType.DEPENDENT);
		try {
			JobKey[] jobKeys = jobManager.getJobKeys(jobQuery);
			if (ArrayUtils.isNotEmpty(jobKeys)) {
				JobKey[] dependentKeys;
				for (JobKey jobKey : jobKeys) {
					// add listener to watch parallel dependency job done
					dependentKeys = jobManager.getDependentKeys(jobKey, DependencyType.PARALLEL);
					if (ArrayUtils.isNotEmpty(dependentKeys)) {
						for (JobKey dependency : dependentKeys) {
							jobRuntimeListenerContainer.addListener(dependency,
									ApplicationContextUtils.instantiateClass(JobParallelizationListener.class));
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
