package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.multithreads.ExecutorUtils;

/**
 * 
 * ForkJoinProcessPoolExecutor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ForkJoinProcessPoolExecutor extends ProcessPoolExecutor implements ForkJoinProcessPool {

	private final ForkJoinPool pool;

	ForkJoinProcessPoolExecutor() {
		this.pool = ForkJoinPool.commonPool();
	}

	ForkJoinProcessPoolExecutor(int parallelism) {
		this.pool = new ForkJoinPool(parallelism);
	}

	@Autowired
	private MultiProcessingMethodDetector methodDetector;

	public <T> Future<T> submit(Signature signature, ForkJoinProcess<T> process) {
		return pool.submit(new ForkJoinFrame<T>(signature, process));
	}

	public <T> Future<T> submit(String serviceName, ForkJoinProcess<T> process) {
		Signature signature = methodDetector.getSignature(serviceName);
		return pool.submit(new ForkJoinFrame<T>(signature, process));
	}

	public void shutdown() {
		super.shutdown();
		ExecutorUtils.gracefulShutdown(pool, 60000);
	}

}
