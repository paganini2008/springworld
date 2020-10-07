package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

/**
 * 
 * ForkJoinFrame
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ForkJoinFrame<T> extends RecursiveTask<T> {

	private static final long serialVersionUID = 769151212202818675L;

	ForkJoinFrame(Signature signature, ForkJoinProcess<T> process) {
		this.signature = signature;
		this.process = process;
	}

	private final Signature signature;
	private final ForkJoinProcess<T> process;

	public ForkJoinTask<T> fork(ForkJoinProcess<T> subProcess) {
		return new RecursiveTaskAdaptor(subProcess).fork();
	}

	@SuppressWarnings("unchecked")
	public T call(Object... arguments) {
		Object bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
		return (T) MethodUtils.invokeMethod(bean, signature.getMethodName(), arguments);
	}

	@Override
	protected final T compute() {
		return process.process(this);
	}

	/**
	 * 
	 * RecursiveTaskAdaptor
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	public class RecursiveTaskAdaptor extends RecursiveTask<T> {

		private static final long serialVersionUID = 1L;
		final ForkJoinProcess<T> subProcess;

		RecursiveTaskAdaptor(ForkJoinProcess<T> subProcess) {
			this.subProcess = subProcess;
		}

		@Override
		protected T compute() {
			return subProcess.process(ForkJoinFrame.this);
		}

	}

}
