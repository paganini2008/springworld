package com.github.paganini2008.springdessert.xtransport.transport;

import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageHandler;
import com.github.paganini2008.springdessert.xtransport.NioServerStartPromise;
import com.github.paganini2008.springdessert.xtransport.ServerInfo;

/**
 * 
 * NioServerStarterListener
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class NioServerStarterListener implements RedisMessageHandler {

	private final Observable observable = Observable.unrepeatable();

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	public void addPromise(NioServerStartPromise startPromise) {
		observable.addObserver((ob, arg) -> {
			startPromise.afterServerStarted((ServerInfo) arg);
		});
	}

	public int countOfPromise() {
		return observable.countOfObservers();
	}

	@Override
	public String getChannel() {
		return String.format(NioServerStarter.DEFAULT_CHANNEL_PATTERN, clusterName);
	}

	@Override
	public void onMessage(String channel, Object message) throws Exception {
		observable.notifyObservers((ServerInfo) message);
	}

}
