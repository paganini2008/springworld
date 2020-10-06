package com.github.paganini2008.springworld.cluster.pool;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.utils.BeanExpressionUtils;

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

	private static final List<Class<?>> componentTypes = Collections
			.synchronizedList(Arrays.asList(Component.class, Repository.class, Service.class));

	@Autowired
	private ProcessPool processPool;

	@Autowired
	private InvocationBarrier invocationBarrier;

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(com.github.paganini2008.springworld.cluster.pool.MultiProcessing)")
	public Object arround(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Method method = signature.getMethod();
		MultiProcessing anno = method.getAnnotation(MultiProcessing.class);
		if (invocationBarrier.isCompleted()) {
			try {
				return pjp.proceed();
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				if (ignoreException(e, anno.ignoredFor())) {
					if (StringUtils.isNotBlank(anno.defaultValue())) {
						return BeanExpressionUtils.resolveExpression(anno.defaultValue(), method.getReturnType());
					}
				}
				throw e;
			}
		} else {
			Class<?> beanClass = signature.getDeclaringType();
			String beanName = getBeanName(beanClass);
			String methodName = method.getName();
			Object[] arguments = pjp.getArgs();

			try {
				if (anno.async()) {
					processPool.execute(beanName, beanClass, methodName, arguments);
					return null;
				} else {
					TaskPromise promise = processPool.submit(beanName, beanClass, methodName, arguments);
					Supplier<Object> defaultValue = StringUtils.isNotBlank(anno.defaultValue()) ? () -> {
						return BeanExpressionUtils.resolveExpression(anno.defaultValue(), method.getReturnType());
					} : null;
					return anno.timeout() > 0 ? promise.get(anno.timeout(), TimeUnit.MILLISECONDS, defaultValue)
							: promise.get(defaultValue);
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				throw e;
			}
		}
	}

	private String getBeanName(Class<?> beanClass) {
		for (Annotation annotation : beanClass.getAnnotations()) {
			if (componentTypes.contains(annotation.annotationType())) {
				String componentName;
				if (StringUtils.isNotBlank(componentName = getComponentName(annotation))) {
					return componentName;
				}
			}
		}
		return null;
	}

	private String getComponentName(Annotation componentAnnotation) {
		if (componentAnnotation instanceof Component) {
			return ((Component) componentAnnotation).value();
		} else if (componentAnnotation instanceof Service) {
			return ((Service) componentAnnotation).value();
		} else if (componentAnnotation instanceof Repository) {
			return ((Repository) componentAnnotation).value();
		}
		return null;
	}

	private boolean ignoreException(Throwable e, Class<?>[] ignoredExceptionClasses) {
		if (ArrayUtils.isNotEmpty(ignoredExceptionClasses)) {
			for (Class<?> exceptionClass : ignoredExceptionClasses) {
				if (exceptionClass.isAssignableFrom(e.getClass())) {
					return true;
				}
			}
		}
		return false;
	}

}
