package com.github.paganini2008.springworld.joblink.cron4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ErrorHandler;

import com.github.paganini2008.devtools.cron4j.Task;
import com.github.paganini2008.devtools.proxy.Aspect;
import com.github.paganini2008.devtools.proxy.JdkProxyFactory;
import com.github.paganini2008.devtools.proxy.ProxyFactory;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.cluster.pool.ForkJoinProcess;
import com.github.paganini2008.springworld.cluster.pool.ForkJoinProcessPool;
import com.github.paganini2008.springworld.cluster.pool.ForkJoinTask;
import com.github.paganini2008.springworld.cluster.pool.MethodSignature;
import com.github.paganini2008.springworld.joblink.BeanNames;
import com.github.paganini2008.springworld.joblink.Job;
import com.github.paganini2008.springworld.joblink.JobExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JobParallelizationForCron4j
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JobParallelizationForCron4j implements Task {

	private static final String ENHANCED_METHOD_NAME = "execute";
	private static final ProxyFactory PROXY_FACTORY = new JdkProxyFactory();

	private final Job job;
	private final Object attachment;

	JobParallelizationForCron4j(Job job, Object attachment) {
		this.job = job;
		this.attachment = attachment;
	}

	@Qualifier("scheduler-error-handler")
	@Autowired
	private ErrorHandler errorHandler;

	@Qualifier(BeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private ForkJoinProcessPool pool;

	@Override
	public boolean execute() {
		Job jobProxy = (Job) PROXY_FACTORY.getProxy(job, new JobParallelizationEnhancer(), Job.class);
		jobExecutor.execute(jobProxy, attachment, 0);
		return true;
	}

	@Override
	public boolean onError(Throwable e) {
		errorHandler.handleError(e);
		return true;
	}

	private class JobParallelizationEnhancer implements Aspect {

		@Override
		public Object call(Object target, Method method, Object[] args) {
			if (ENHANCED_METHOD_NAME.equals(method.getName())) {
				MethodSignature signature = (MethodSignature) MethodSignature.of(method);
				signature.setBeanName(job.getJobName());
				Future<Object> future = pool.submit(signature, new JobParallelizationProcess());
				try {
					return future.get();
				} catch (Throwable e) {
					log.error(e.getMessage(), e);
				}
				return null;
			}
			return MethodUtils.invokeMethod(target, method, args);
		}

	}

	private class JobParallelizationProcess implements ForkJoinProcess<Object> {

		@Override
		public Object[] process(ForkJoinTask<Object> task) {
			Object[] arguments = job.getParallelPolicy().slice(attachment);
			List<Object> results = new ArrayList<Object>(arguments.length);
			for (Object argument : arguments) {
				results.add(task.call(argument));
			}
			return results.toArray();
		}

	}

}
