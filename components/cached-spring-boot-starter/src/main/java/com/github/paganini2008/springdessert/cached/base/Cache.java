package com.github.paganini2008.springdessert.cached.base;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Cache
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface Cache {

	/**
	 * 
	 * Signature
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	interface Signature {

		Object getProxy();

	}

	/**
	 * 
	 * Operation of map
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	interface HashOperations extends Signature {

		@Sort
		void set(String key, String name, Object object);

		@Sort
		void append(String key, Map<String, Object> m);

		@Hit
		@Sort
		boolean hasKey(String key, String name);

		@Hit
		@Sort
		Object get(String key, String name, Object defaultValue);

		@Hit
		@Sort
		Map<String, Object> get(String key);

		@Sort
		void delete(String key, String name);
	}

	/**
	 * 
	 * Opeation of set
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	interface SetOperations extends Signature {

		@Sort
		void add(String key, Object object);

		@Sort
		void append(String key, Collection<Object> c);

		@Sort
		Object pollFirst(String key);

		@Sort
		Object pollLast(String key);

		@Hit
		@Sort
		Object peekFirst(String key);

		@Hit
		@Sort
		Object peekLast(String key);

		@Hit
		@Sort
		List<Object> list(String key);

	}

	/**
	 * 
	 * Operation of list
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	interface ListOperations extends Signature {

		@Sort
		void append(String key, Collection<Object> c);

		@Sort
		void addFirst(String key, Object object);

		@Sort
		void addLast(String key, Object object);

		@Hit
		@Sort
		List<Object> list(String key);

		@Hit
		@Sort
		Object peekFirst(String key);

		@Hit
		@Sort
		Object peekLast(String key);

		@Sort
		Object pollFirst(String key);

		@Sort
		Object pollLast(String key);

	}

	/**
	 * 
	 * Operation of value
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	interface ValueOperations extends Signature {

		@Sort
		void set(String key, Object object);

		@Hit
		@Sort
		boolean hasKey(String key);

		@Hit
		@Sort
		Object get(String key);

		@Hit
		@Sort
		long longValue(String key);

		@Sort
		long increment(String key);

		@Sort
		long decrement(String key);

		@Sort
		long addLong(String key, long delta);

		@Hit
		@Sort
		double doubleValue(String key);

		@Sort
		double addDouble(String key, double delta);

		@Delete
		default void delete(String key) {
			evict(key, RemovalReason.INVALIDATION);
		}

		@Delete
		void evict(String key, RemovalReason removalReason);

		@Clear
		void clear();

		Set<String> keys();

		int size();
	}

	void set(String key, Object object);

	boolean hasKey(String key);

	Object get(String key);

	long longValue(String key);

	long increment(String key);

	long decrement(String key);

	long addLong(String key, long delta);

	double doubleValue(String key);

	double addDouble(String key, double delta);

	Set<String> keys();

	default boolean isEmpty() {
		return size() == 0;
	}

	int size();

	default void delete(String key) {
		evict(key, RemovalReason.INVALIDATION);
	}

	void evict(String key, RemovalReason removalReason);

	void clear();

	HashOperations hash();

	SetOperations set();

	ListOperations list();

	default void close() {
	}
}