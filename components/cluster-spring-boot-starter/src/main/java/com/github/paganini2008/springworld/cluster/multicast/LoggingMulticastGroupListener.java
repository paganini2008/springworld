package com.github.paganini2008.springworld.cluster.multicast;

import com.github.paganini2008.springworld.cluster.ApplicationInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * LoggingMulticastGroupListener
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class LoggingMulticastGroupListener implements MulticastGroupListener {

	@Override
	public void onActive(ApplicationInfo applicationInfo) {
		if (log.isTraceEnabled()) {
			log.trace("Spring application '{}' has joined.", applicationInfo);
		}
	}

	@Override
	public void onInactive(ApplicationInfo applicationInfo) {
		if (log.isTraceEnabled()) {
			log.trace("Spring application '{}' has gone.", applicationInfo);
		}
	}

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
		if (log.isTraceEnabled()) {
			log.trace("Spring application '{}' send message: {}", applicationInfo.getId(), message);
		}
	}

}
