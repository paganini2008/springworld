package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ForkJoinInterpreter
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@Aspect
public class ForkJoinInterpreter {

	@Autowired
	private MultiProcessingMethodDetector methodDetector;

	@Autowired
	private ForkJoinPool pool;

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(forkJoin)")
	public Object arround(ProceedingJoinPoint pjp, ForkJoin forkJoin) throws Throwable {
		Signature signature = methodDetector.getSignature(forkJoin.value());
		Object bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
		if (bean == null) {
			throw new NoSuchBeanDefinitionException(signature.getBeanName());
		}
		try {
			MultiProcessingRecursiveTask<?> recursiveTask = ApplicationContextUtils
					.autowireBean(BeanUtils.instantiate(forkJoin.usingTask()));
			RecursiveTaskEnhancer taskEnhancer = new RecursiveTaskEnhancer(bean, recursiveTask, signature);
			recursiveTask = taskEnhancer.enhance();
			Future<?> future = pool.submit(recursiveTask);
			return future.get();
		} catch (Throwable e) {
			if (ExceptionUtils.ignoreException(e, forkJoin.ignoreFor())) {
				log.error(e.getMessage(), e);
				return pjp.proceed();
			}
			throw e;
		}
	}

}
