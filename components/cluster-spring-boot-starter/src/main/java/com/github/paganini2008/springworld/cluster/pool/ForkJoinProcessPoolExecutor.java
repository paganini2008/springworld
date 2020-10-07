package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import com.github.paganini2008.devtools.multithreads.ExecutorUtils;

/**
 * 
 * ForkJoinProcessPoolExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ForkJoinProcessPoolExecutor implements ForkJoinProcessPool {

	private final Signature signature;
	private final ForkJoinPool pool;

	ForkJoinProcessPoolExecutor(Signature signature) {
		this.signature = signature;
		this.pool = ForkJoinPool.commonPool();
	}

	ForkJoinProcessPoolExecutor(Signature signature, int parallelism) {
		this.signature = signature;
		this.pool = new ForkJoinPool(parallelism);
	}

	public <T> T submit(ForkJoinProcess<T> process) throws Exception {
		Future<T> future = pool.submit(new ForkJoinFrame<T>(signature, process));
		return future.get();
	}

	public void close() {
		ExecutorUtils.gracefulShutdown(pool, 60000);
	}

}
