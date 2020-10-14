package com.github.paganini2008.springworld.cluster.multicast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * ClusterMulticastController
 * 
 * @author Fred Feng
 * @version 1.0
 */
@RequestMapping("/application/cluster")
@RestController
@ConditionalOnProperty(value = "spring.application.cluster.multicast.enabled", havingValue = "true")
public class ClusterMulticastController {

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Value("${spring.application.name}")
	private String applicationName;

	@GetMapping("/multicast")
	public ResponseEntity<String> multicast(@RequestParam(name = "t", required = false, defaultValue = "*") String topic,
			@RequestParam("c") String content) {
		clusterMulticastGroup.multicast(topic, content);
		return ResponseEntity.ok("ok");
	}

	@GetMapping("/unicast")
	public ResponseEntity<String> unicast(@RequestParam(name = "t", required = false, defaultValue = "*") String topic,
			@RequestParam("c") String content) {
		clusterMulticastGroup.unicast(topic, content);
		return ResponseEntity.ok("ok");
	}

}
