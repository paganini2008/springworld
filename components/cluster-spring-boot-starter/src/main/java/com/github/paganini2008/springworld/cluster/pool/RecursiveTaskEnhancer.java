package com.github.paganini2008.springworld.cluster.pool;

import java.lang.reflect.Method;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import com.github.paganini2008.devtools.reflection.MethodUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RecursiveTaskEnhancer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class RecursiveTaskEnhancer implements MethodInterceptor {

	private static final String ENHANCED_METHOD_NAME = "call";

	private final Signature signature;
	private final Object bean;
	private final Object delegate;

	RecursiveTaskEnhancer(Object bean, Object delegate, Signature signature) {
		this.bean = bean;
		this.delegate = delegate;
		this.signature = signature;
	}

	@Override
	public Object intercept(Object o, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
		if (method.getName().equals(ENHANCED_METHOD_NAME)) {
			try {
				return MethodUtils.invokeMethod(bean, signature.getMethodName(), arguments);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return MethodUtils.invokeMethod(delegate, method, arguments);
			}
		}
		return MethodUtils.invokeMethod(delegate, method, arguments);
	}

	public MultiProcessingRecursiveTask<?> enhance() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(MultiProcessingRecursiveTask.class);
		enhancer.setCallback(this);
		return (MultiProcessingRecursiveTask<?>) enhancer.create();
	}

}
