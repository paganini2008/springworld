package com.github.paganini2008.springdessert.cluster;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springdessert.cluster.election.LeaderElection;
import com.github.paganini2008.springdessert.reditools.BeanNames;

/**
 * 
 * ApplicationClusterController
 * 
 * @author Fred Feng
 * @version 1.0
 */
@RequestMapping("/application/cluster")
@RestController
public class ApplicationClusterController {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private LeaderContext leaderContext;

	@Autowired
	private LeaderElection leaderElection;

	@GetMapping("/ping")
	public ResponseEntity<ApplicationInfo> ping() {
		return ResponseEntity.ok(instanceId.getApplicationInfo());
	}

	@GetMapping("/state")
	public ResponseEntity<ClusterState> state() {
		return ResponseEntity.ok(leaderContext.getClusterState());
	}

	@GetMapping("/list")
	public ResponseEntity<ApplicationInfo[]> list() {
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		List<Object> dataList = redisTemplate.opsForList().range(key, 0, -1);
		ApplicationInfo[] infos = ArrayUtils.cast(dataList.toArray(), ApplicationInfo.class);
		return ResponseEntity.ok(infos);
	}

	@GetMapping("/recovery")
	public ResponseEntity<String> recovery() {
		leaderElection.launch();
		return ResponseEntity.ok("ok");
	}

}
