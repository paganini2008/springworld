package com.github.paganini2008.springworld.cluster.pool;

import java.util.ArrayList;
import java.util.List;

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
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ParallelizingInterpreter
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@Aspect
public class CallParallelizingInterpreter {

	@Autowired
	private MultiProcessingMethodDetector methodDetector;

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(parallelizing)")
	public Object arround(ProceedingJoinPoint pjp, CallParallelizing parallelizing) throws Throwable {
		if (((org.aspectj.lang.reflect.MethodSignature) pjp.getSignature()).getMethod().isAnnotationPresent(MultiProcessing.class)) {
			throw new UnsupportedOperationException(
					"Either annotation 'MethodParallelizing' or 'MultiProcessing' is to decorate on target method.");
		}
		Object[] args = pjp.getArgs();
		if (ArrayUtils.isEmpty(args)) {
			throw new IllegalArgumentException("No arguments");
		}

		Signature signature = methodDetector.getSignature(parallelizing.value());
		Object bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
		if (bean == null) {
			throw new NoSuchBeanDefinitionException(signature.getBeanName());
		}
		try {
			List<Object> results = new ArrayList<Object>();
			CallParallelization parallelization = BeanUtils.instantiate(parallelizing.usingParallelization());
			parallelization = ApplicationContextUtils.autowireBean(parallelization);
			Object[] slices = parallelization.slice(args[0]);
			for (Object slice : slices) {
				Object result = MethodUtils.invokeMethod(bean, signature.getMethodName(), slice);
				results.add(result);
			}
			return parallelization.merge(results.toArray());
		} catch (Throwable e) {
			if (ExceptionUtils.ignoreException(e, parallelizing.ignoreFor())) {
				log.error(e.getMessage(), e);
				return pjp.proceed();
			}
			throw e;
		}

	}

}
