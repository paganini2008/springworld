package com.github.paganini2008.springworld.cluster;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private Environment env;

	@GetMapping("/info")
	public Map<String, Object> info() {
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("instanceId", instanceId.get());
		info.put("leader", instanceId.isLeader());
		info.put("port", env.getProperty("server.port"));
		return info;
	}

}
