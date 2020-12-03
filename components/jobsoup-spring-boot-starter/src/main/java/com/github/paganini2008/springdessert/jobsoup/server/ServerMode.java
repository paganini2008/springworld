package com.github.paganini2008.springdessert.jobsoup.server;

/**
 * 
 * ServerMode
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public enum ServerMode {

	CONSUMER("consumer"), PRODUCER("producer");

	private final String value;

	private ServerMode(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
