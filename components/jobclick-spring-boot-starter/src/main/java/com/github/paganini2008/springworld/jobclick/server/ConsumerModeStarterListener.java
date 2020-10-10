package com.github.paganini2008.springworld.jobclick.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.springworld.cluster.ApplicationClusterRefreshedEvent;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.jobclick.model.JobResult;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsumerModeStarterListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ConsumerModeStarterListener implements ApplicationListener<ApplicationClusterRefreshedEvent> {

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private ConsumerModeRestTemplate restTemplate;

	@Override
	public void onApplicationEvent(ApplicationClusterRefreshedEvent event) {
		final ApplicationInfo applicationInfo = instanceId.getApplicationInfo();
		ResponseEntity<JobResult<Boolean>> responseEntity = restTemplate.perform(null, "/job/admin/registerCluster", HttpMethod.POST,
				applicationInfo, new ParameterizedTypeReference<JobResult<Boolean>>() {
				});
		if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody().getData()) {
			log.info("'{}' register to job producer ok.", applicationInfo);
		}
	}

}
