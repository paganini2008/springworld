package com.github.paganini2008.springworld.cluster.pool;

import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * MultiProcessingInterpreter
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@Aspect
public class MultiProcessingInterpreter {

	@Autowired
	private ProcessPool processPool;

	@Autowired
	private InvocationBarrier invocationBarrier;

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(com.github.paganini2008.springworld.cluster.pool.MultiProcessing)")
	public Object arround(ProceedingJoinPoint pjp) throws Throwable {
		if (invocationBarrier.isCompleted()) {
			return pjp.proceed();
		} else {
			MethodSignature signature = (MethodSignature) pjp.getSignature();
			MultiProcessing anno = signature.getMethod().getAnnotation(MultiProcessing.class);
			Class<?> beanClass = signature.getDeclaringType();
			Component component = beanClass.getAnnotation(Component.class);

			String beanName = component.value();
			String methodName = pjp.getSignature().getName();
			Object[] arguments = pjp.getArgs();
			
			try {
				if (anno.async()) {
					processPool.execute(beanName, beanClass, methodName, arguments);
					return null;
				} else {
					TaskPromise promise = processPool.submit(beanName, beanClass, methodName, arguments);
					return anno.timeout() > 0 ? promise.get(anno.timeout(), TimeUnit.MILLISECONDS) : promise.get();
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				throw e;
			}
		}
	}

}
