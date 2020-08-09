package com.github.paganini2008.springworld.scheduler;

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

	long getNextExectionTime();
	
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
		public long getNextExectionTime() {
			return System.currentTimeMillis();
		}
		
		@Override
		public void cancel() {
		}
	};

}
