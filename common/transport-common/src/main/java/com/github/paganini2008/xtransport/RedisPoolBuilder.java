package com.github.paganini2008.xtransport;

/**
 * 
 * RedisPoolBuilder
 *
 * @author Jimmy Hoff
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
