package com.github.paganini2008.springdessert.cluster.multicast;

import com.github.paganini2008.springdessert.cluster.ApplicationInfo;

/**
 * 
 * ApplicationMessageListener
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public interface ApplicationMessageListener extends ApplicationClusterListener {

	void onMessage(ApplicationInfo applicationInfo, String id, Object message);

	default String getTopic() {
		return "*";
	}

}
