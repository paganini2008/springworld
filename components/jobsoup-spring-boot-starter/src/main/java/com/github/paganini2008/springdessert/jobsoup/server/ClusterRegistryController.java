package com.github.paganini2008.springdessert.jobsoup.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.jobsoup.model.JobResult;

/**
 * 
 * ClusterRegistryController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/job/admin")
public class ClusterRegistryController {

	@Autowired
	private ClusterRegistry clusterRegistry;

	@PostMapping("/registerCluster")
	public ResponseEntity<JobResult<Boolean>> registerCluster(@RequestBody ApplicationInfo applicationInfo) {
		clusterRegistry.registerCluster(applicationInfo);
		return ResponseEntity.ok(JobResult.success(Boolean.TRUE));
	}

}
