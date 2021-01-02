package com.github.paganini2008.springdessert.xtransport.transport;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;
import com.github.paganini2008.springdessert.xtransport.ServerInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * NioServerStarter
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class NioServerStarter implements ApplicationListener<ContextRefreshedEvent>, InitializingBean, Executable {

	public static final String DEFAULT_CHANNEL_PATTERN = "spring:application:cluster:%s:transport:starter";

	@Autowired
	private NioServer nioServer;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private NioServerStarterListener starterListener;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	private InetSocketAddress socketAddress;

	@Override
	public void afterPropertiesSet() throws Exception {
		ThreadUtils.scheduleWithFixedDelay(this, 5, TimeUnit.SECONDS);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			socketAddress = (InetSocketAddress) nioServer.start();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean execute() {
		if (socketAddress != null && starterListener.countOfPromise() > 0) {
			final String listeningChannel = String.format(DEFAULT_CHANNEL_PATTERN, clusterName);
			redisMessageSender.sendMessage(listeningChannel, new ServerInfo(socketAddress));
		}
		return socketAddress == null || nioServer.isStarted();
	}

}
