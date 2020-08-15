package com.github.paganini2008.springworld.scheduler;

import java.util.Date;

/**
 * 
 * JobFuture
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface JobFuture {

	void cancel();

	boolean isDone();

	boolean isCancelled();

	long getNextExectionTime(Date lastExecutionTime, Date lastActualExecutionTime, Date lastCompletionTime);

	static final JobFuture EMPTY = new JobFuture() {

		@Override
		public boolean isDone() {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public long getNextExectionTime(Date lastExecutionTime, Date lastActualExecutionTime, Date lastCompletionTime) {
			return System.currentTimeMillis();
		}

		@Override
		public void cancel() {
		}
	};

}
