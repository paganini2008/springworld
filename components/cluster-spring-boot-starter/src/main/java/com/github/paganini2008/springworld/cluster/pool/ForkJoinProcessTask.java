package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ForkJoinProcessTask
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ForkJoinProcessTask<T> extends RecursiveTask<T> {

	private static final long serialVersionUID = 1L;
	private final ForkJoinProcess<T> subProcess;
	private final ForkJoinFrame<T> forkJoinFrame;

	ForkJoinProcessTask(ForkJoinProcess<T> subProcess, ForkJoinFrame<T> forkJoinFrame) {
		this.subProcess = subProcess;
		this.forkJoinFrame = forkJoinFrame;
	}

	private Supplier<T> defaultValue;

	public void setDefaultValue(Supplier<T> defaultValue) {
		this.defaultValue = defaultValue;
	}

	public ForkJoinProcess<T> getSubProcess() {
		return subProcess;
	}

	@Override
	protected T compute() {
		long startTime = System.currentTimeMillis();
		try {
			return subProcess.process(forkJoinFrame);
		} catch (Throwable e) {
			if (defaultValue != null) {
				log.error(e.getMessage(), e);
				return defaultValue.get();
			} else {
				throw e;
			}
		} finally {
			if (log.isTraceEnabled()) {
				log.trace("SubProcess: {}, Take: {} ms.", subProcess, System.currentTimeMillis() - startTime);
			}
		}
	}

}
