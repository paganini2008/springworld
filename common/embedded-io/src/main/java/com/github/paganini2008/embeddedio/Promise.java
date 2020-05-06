package com.github.paganini2008.embeddedio;

/**
 * 
 * Promise
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface Promise<T> {

	void onSuccess(T object);

	void onFailure(Throwable e);

}
