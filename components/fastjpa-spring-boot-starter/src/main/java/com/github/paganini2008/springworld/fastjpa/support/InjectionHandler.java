package com.github.paganini2008.springworld.fastjpa.support;

/**
 * 
 * InjectionHandler
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface InjectionHandler {

	Object inject(Object original, String targetProperty, Class<?> targetPropertyType);

}
