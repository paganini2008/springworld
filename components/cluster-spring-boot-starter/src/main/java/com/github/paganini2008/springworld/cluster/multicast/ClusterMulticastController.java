package com.github.paganini2008.springworld.cluster.multicast;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
	private ClusterMulticastGroup multicastGroup;

	@Value("${spring.application.name}")
	private String applicationName;

	@GetMapping("/multicast")
	public Map<String, Object> multicast(@RequestParam("c") String content) {
		multicastGroup.multicast("*", content);
		return resultMap(content);
	}

	@GetMapping("/unicast")
	public Map<String, Object> unicast(@RequestParam("c") String content) {
		multicastGroup.unicast("*", content);
		return resultMap(content);
	}

	@GetMapping("/unicast2")
	public Map<String, Object> unicast2(@RequestParam("c") String content) {
		multicastGroup.unicast(applicationName, "*", content);
		return resultMap(content);
	}

	private Map<String, Object> resultMap(String content) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("data", content);
		data.put("success", true);
		return data;
	}

}
