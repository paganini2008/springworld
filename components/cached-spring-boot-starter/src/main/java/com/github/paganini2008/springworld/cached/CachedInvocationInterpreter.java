package com.github.paganini2008.springworld.cached;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CachedInvocationInterpreter
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
@Aspect
public class CachedInvocationInterpreter {

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(com.github.paganini2008.springworld.cluster.cache.Cached)")
	public Object arround(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Cached cachedInfo = signature.getMethod().getAnnotation(Cached.class);
		Cache cache = ApplicationContextUtils.getBean(cachedInfo.ref(), Cache.class);
		if (cache == null) {
			if (log.isTraceEnabled()) {
				log.trace("Can not find cache instance by name: " + cachedInfo.ref());
			}
			return pjp.proceed();
		}

		String key = cachedInfo.value();
		if (StringUtils.isBlank(key)) {
			StringBuilder keyRepr = new StringBuilder();
			Class<?> beanClass = signature.getDeclaringType();
			Component component = beanClass.getAnnotation(Component.class);
			String beanName = component.value();
			if (StringUtils.isBlank(beanName)) {
				beanName = beanClass.getName();
			}
			keyRepr.append(beanName);
			String methodName = signature.getMethod().getName();
			keyRepr.append(".").append(methodName);
			Object[] arguments = pjp.getArgs();
			keyRepr.append("(").append(ArrayUtils.join(arguments, ",")).append(")");
			key = keyRepr.toString();
		}
		Object result = cache.get(key);
		if (result == null) {
			result = pjp.proceed();
			cache.set(key, result);
		}
		return result;
	}

}
