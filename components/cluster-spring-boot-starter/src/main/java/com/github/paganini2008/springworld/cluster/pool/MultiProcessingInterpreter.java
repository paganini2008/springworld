package com.github.paganini2008.springworld.cluster.pool;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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
			Class<?> beanClass = pjp.getSignature().getDeclaringType();
			Component component = beanClass.getAnnotation(Component.class);
			
			String beanName = component.value();
			String methodName = pjp.getSignature().getName();
			Object[] arguments = pjp.getArgs();
			try {
				processPool.submit(beanName, beanClass, methodName, arguments);
				return null;
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				throw e;
			}
		}
	}

}
