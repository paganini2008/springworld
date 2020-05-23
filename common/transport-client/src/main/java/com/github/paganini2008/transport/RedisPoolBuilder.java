package com.github.paganini2008.transport;

/**
 * 
 * RedisPoolBuilder
 *
 * @author Fred Feng
 * @since 1.0
 */
@FunctionalInterface
public interface RedisPoolBuilder<T> {

	default RedisPoolBuilder<T> setHost(String host) {
		throw new UnsupportedOperationException();
	}

	default RedisPoolBuilder<T> setPort(int port) {
		throw new UnsupportedOperationException();
	}

	default RedisPoolBuilder<T> setAuth(String auth) {
		throw new UnsupportedOperationException();
	}

	default RedisPoolBuilder<T> setDbIndex(int dbIndex) {
		throw new UnsupportedOperationException();
	}

	T createPool();

}
