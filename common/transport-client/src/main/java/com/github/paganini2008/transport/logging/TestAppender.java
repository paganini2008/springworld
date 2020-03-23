package com.github.paganini2008.transport.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * 
 * TestAppender
 *
 * @author Fred Feng
 * @version 1.0
 */
public class TestAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
	
	public TestAppender() {
		System.out.println("Init Init Init Init Init!!!"); 
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		System.out.println("LogItem: " + eventObject.getFormattedMessage());
		System.out.println("Properties: " + clusterName + "\t" + password + "\t" + startupDelay);
	}

	private String clusterName;
	private String password;
	private int startupDelay;

	public int getStartupDelay() {
		return startupDelay;
	}

	public void setStartupDelay(int startupDelay) {
		this.startupDelay = startupDelay;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void start() {
		System.out.println("TestAppender.start()");
		super.start();
	}

	@Override
	public void stop() {
		System.out.println("TestAppender.stop()");
		super.stop();
	}

	@Override
	public boolean isStarted() {
		System.out.println("TestAppender.isStarted()");
		return super.isStarted();
	}

}
