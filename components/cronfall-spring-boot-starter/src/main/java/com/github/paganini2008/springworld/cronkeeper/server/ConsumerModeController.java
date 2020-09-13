package com.github.paganini2008.springworld.cronkeeper.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cronkeeper.JobAdmin;
import com.github.paganini2008.springworld.cronkeeper.JobState;
import com.github.paganini2008.springworld.cronkeeper.model.JobParam;
import com.github.paganini2008.springworld.cronkeeper.model.JobResult;

/**
 * 
 * ConsumerModeController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/job/admin")
public class ConsumerModeController {

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private ConsumerModeRestTemplate restTemplate;

	@Autowired
	private JobAdmin jobAdmin;

	@GetMapping("/registerCluster")
	public ResponseEntity<JobResult<Boolean>> registerCluster() throws Exception {
		ApplicationInfo applicationInfo = instanceId.getApplicationInfo();
		return restTemplate.perform(null, "/job/admin/registerCluster", HttpMethod.POST, applicationInfo,
				new ParameterizedTypeReference<JobResult<Boolean>>() {
				});
	}

	@PostMapping("/triggerJob")
	public ResponseEntity<JobResult<JobState>> triggerJob(@RequestBody JobParam jobParam) throws Exception {
		JobState jobState = jobAdmin.triggerJob(jobParam.getJobKey(), jobParam.getAttachment());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

}
