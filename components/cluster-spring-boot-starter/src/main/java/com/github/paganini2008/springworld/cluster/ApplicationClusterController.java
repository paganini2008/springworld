package com.github.paganini2008.springworld.cluster;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.springworld.reditools.BeanNames;

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

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Autowired
	private InstanceId instanceId;

	@GetMapping("/info")
	public ResponseEntity<ApplicationInfo[]> info() {
		final String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		List<Object> dataList = redisTemplate.opsForList().range(key, 0, -1);
		ApplicationInfo[] results = new ApplicationInfo[dataList != null ? dataList.size() : 0];
		int i = 0;
		ApplicationInfo applicationInfo;
		for (Object data : dataList) {
			applicationInfo = (ApplicationInfo) data;
			applicationInfo
					.setLeader(instanceId.getLeaderInfo() != null && instanceId.getLeaderInfo().getId().equals(applicationInfo.getId()));
			results[i++] = applicationInfo;
		}
		return ResponseEntity.ok(results);
	}

}
