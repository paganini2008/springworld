package com.github.paganini2008.springdessert.xtransport;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.multicast.MulticastGroupListener;
import com.github.paganini2008.xtransport.NioClient;
import com.github.paganini2008.xtransport.TransportNodeCentre;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ScaleoutClusterListener
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class ScaleoutClusterListener implements MulticastGroupListener {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private NioClient nioClient;

	@Autowired
	private TransportNodeCentre transportNodeCentre;

	@Override
	public void onActive(ApplicationInfo applicationInfo) {
		String instanceId = applicationInfo.getId();
		log.info("New node '{}' join spring transport cluster '{}'", applicationInfo, clusterName);
		String location = (String) transportNodeCentre.findNode(instanceId);
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
	public void onInactive(ApplicationInfo applicationInfo) {
		log.info("Node '{}' has left spring transport cluster '{}'", applicationInfo, clusterName);
	}

}
