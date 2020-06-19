package com.github.paganini2008.springworld.cache;

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

		@Order
		void set(String key, String name, Object object);

		@Order
		void append(String key, Map<String, Object> m);

		@Hit
		@Order
		boolean hasKey(String key, String name);

		@Hit
		@Order
		Object get(String key, String name);

		@Hit
		@Order
		Map<String, Object> get(String key);

		@Order
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

		@Order
		void add(String key, Object object);

		@Order
		void append(String key, Collection<Object> c);

		@Order
		Object pollFirst(String key);

		@Order
		Object pollLast(String key);

		@Hit
		@Order
		Object peekFirst(String key);

		@Hit
		@Order
		Object peekLast(String key);

		@Hit
		@Order
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

		@Order
		void append(String key, Collection<Object> c);

		@Order
		void addFirst(String key, Object object);

		@Order
		void addLast(String key, Object object);

		@Hit
		@Order
		List<Object> list(String key);

		@Hit
		@Order
		Object peekFirst(String key);

		@Hit
		@Order
		Object peekLast(String key);

		@Order
		Object pollFirst(String key);

		@Order
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

		@Order
		void set(String key, Object object);

		@Hit
		@Order
		boolean hasKey(String key);

		@Hit
		@Order
		Object get(String key);

		@Hit
		@Order
		long longValue(String key);

		@Order
		long increment(String key);

		@Order
		long decrement(String key);

		@Order
		long addLong(String key, long delta);

		@Hit
		@Order
		double doubleValue(String key);

		@Order
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