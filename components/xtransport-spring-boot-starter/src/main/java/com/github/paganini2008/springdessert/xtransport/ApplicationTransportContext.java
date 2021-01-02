package com.github.paganini2008.springdessert.xtransport;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastListener;
import com.github.paganini2008.springdessert.xtransport.transport.NioServerStarterListener;
import com.github.paganini2008.xtransport.NioClient;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationTransportContext
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class ApplicationTransportContext implements ApplicationMulticastListener {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private NioServerStarterListener nioServerStarterListener;

	@Autowired
	private NioClient nioClient;

	private final Map<ApplicationInfo, ServerInfo> serverInfos = new ConcurrentHashMap<ApplicationInfo, ServerInfo>();

	public ServerInfo[] getServerInfos() {
		return serverInfos.values().toArray(new ServerInfo[0]);
	}

	@Override
	public void onActive(ApplicationInfo applicationInfo) {
		nioServerStarterListener.addPromise(serverInfo -> {
			nioClient.connect(new InetSocketAddress(serverInfo.getHostName(), serverInfo.getPort()), address -> {
				log.info("NioClient connect to: " + address);
				serverInfos.put(applicationInfo, serverInfo);
			});
		});
	}

	@Override
	public void onInactive(ApplicationInfo applicationInfo) {
		log.info("Application [{}] has left spring transport cluster [{}]", applicationInfo, clusterName);
		serverInfos.remove(applicationInfo);
	}

}
