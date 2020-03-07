package com.github.paganini2008.springworld.tx;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * NoopTransaction
 *
 * @author Fred Feng
 * @version 1.0
 */
public class NoopTransaction implements Transaction {

	private final String id;
	private final long startTime;
	private final AtomicBoolean completed = new AtomicBoolean();

	public NoopTransaction(String id) {
		this.id = id;
		this.startTime = System.currentTimeMillis();
	}

	@Override
	public boolean commit() {
		completed.set(true);
		return true;
	}

	@Override
	public boolean rollback() {
		completed.set(true);
		return true;
	}

	@Override
	public boolean isCompleted() {
		return completed.get();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

}
