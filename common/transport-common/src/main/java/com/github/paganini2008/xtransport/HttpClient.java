package com.github.paganini2008.xtransport;

/**
 * 
 * HttpClient
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface HttpClient extends LifeCycle, Client {

	default boolean isOpened() {
		return true;
	}

	void setConnectionTimeout(int timeout);

	void setReadTimeout(int timeout);

	void addChannel(String channel);

	void clearChannels();

}
