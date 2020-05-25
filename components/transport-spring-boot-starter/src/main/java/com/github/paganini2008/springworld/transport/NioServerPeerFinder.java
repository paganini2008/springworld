package com.github.paganini2008.springworld.transport;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.multicast.ClusterStateChangeListener;
import com.github.paganini2008.transport.NioClient;
import com.github.paganini2008.transport.NodeFinder;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * NioServerPeerFinder
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class NioServerPeerFinder implements ClusterStateChangeListener {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private NioClient nioClient;

	@Autowired
	private NodeFinder nodeFinder;

	@Override
	public void onActive(String instanceId) {
		log.info("Node '{}' join the spring application cluster {}", instanceId, applicationName);
		String location = (String) nodeFinder.findNode(instanceId);
		if (StringUtils.isNotBlank(location)) {
			String[] args = location.split(":", 2);
			String hostName;
			int port;
			try {
				hostName = args[0];
				port = Integer.parseInt(args[1]);
			} catch (RuntimeException e) {
				throw new IllegalArgumentException("Bad parameter: " + location, e);
			}
			nioClient.connect(new InetSocketAddress(hostName, port), address -> {
				log.info("NioClient connect to: " + address);
			});
		}
	}

	@Override
	public void onInactive(String instanceId) {
		log.info("Node '{}' leave the spring application cluster {}", instanceId, applicationName);
	}

}
