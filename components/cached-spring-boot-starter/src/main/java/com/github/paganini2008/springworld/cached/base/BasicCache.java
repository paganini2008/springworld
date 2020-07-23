package com.github.paganini2008.springworld.cached.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.collection.MultiDequeMap;
import com.github.paganini2008.devtools.collection.MultiMappedMap;
import com.github.paganini2008.devtools.collection.MultiSetMap;
import com.github.paganini2008.devtools.multithreads.AtomicDouble;
import com.github.paganini2008.devtools.reflection.MethodUtils;

/**
 * 
 * BasicCache
 *
 * @author Fred Feng
 * @since 1.0
 */
public abstract class BasicCache implements Cache {

	private final Map<String, Object> cache = new ConcurrentHashMap<String, Object>();
	private final Map<String, AtomicLong> longs = new ConcurrentHashMap<String, AtomicLong>();
	private final Map<String, AtomicDouble> doubles = new ConcurrentHashMap<String, AtomicDouble>();
	private final MultiMappedMap<String, String, Object> hash = new MultiMappedMap<String, String, Object>();
	private final MultiSetMap<String, Object> set = new MultiSetMap<String, Object>();
	private final MultiDequeMap<String, Object> list = new MultiDequeMap<String, Object>();

	private final ValueOperations valueOperations = new ValueOperationsImpl();
	private final HashOperations hashOperations = new HashOperaionsImpl();
	private final SetOperations setOperations = new SetOperationsImpl();
	private final ListOperations listOperations = new ListOperationsImpl();

	private final ValueOperations valueOperationsProxy = (ValueOperations) valueOperations.getProxy();
	private final HashOperations hashOperationsProxy = (HashOperations) hashOperations.getProxy();
	private final SetOperations setOperationsProxy = (SetOperations) setOperations.getProxy();
	private final ListOperations listOperationsProxy = (ListOperations) listOperations.getProxy();

	private final Map<Map<String, ?>, Signature> signatures = Collections
			.synchronizedMap(new IdentityHashMap<Map<String, ?>, Signature>(6));

	private KeyExpirationPolicy keyExpirationPolicy = new KeyExpirationPolicy() {
	};
	private RemovalListener removalListener = new RemovalListener() {
	};

	protected BasicCache() {
		signatures.put(cache, valueOperations);
		signatures.put(longs, valueOperations);
		signatures.put(doubles, valueOperations);
		signatures.put(hash, hashOperations);
		signatures.put(set, setOperations);
		signatures.put(list, listOperations);
	}

	public void setKeyExpirationPolicy(KeyExpirationPolicy keyExpirationPolicy) {
		this.keyExpirationPolicy = keyExpirationPolicy;
	}

	public void setRemovalListener(RemovalListener removalListener) {
		this.removalListener = removalListener;
	}

	/**
	 * 
	 * HashOperaionsImpl
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	public class HashOperaionsImpl implements HashOperations, InvocationHandler {

		final HashOperations proxy;

		HashOperaionsImpl() {
			this.proxy = (HashOperations) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { HashOperations.class },
					this);
		}

		@Override
		public void set(String key, String name, Object object) {
			hash.put(key, name, object);
			retain(key, this);
		}

		@Override
		public void append(String key, Map<String, Object> m) {
			hash.append(key, m);
			retain(key, this);
		}

		@Override
		public boolean hasKey(String key, String name) {
			return hash.containsValue(key, name);
		}

		@Override
		public Object get(String key, String name, Object defaultValue) {
			return hash.get(key, name, defaultValue);
		}

		@Override
		public Map<String, Object> get(String key) {
			Map<String, Object> m;
			if ((m = hash.get(key)) != null) {
				return new HashMap<String, Object>(m);
			}
			return null;
		}

		@Override
		public void delete(String key, String name) {
			hash.removeValue(key, name);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
			Object result = MethodUtils.invokeMethod(this, method, arguments);
			if (!method.isAnnotationPresent(Sort.class)) {
				return result;
			}
			final String key = (String) arguments[0];
			keyExpirationPolicy.onSort(key, BasicCache.this);
			return result;
		}

		public HashOperations getProxy() {
			return proxy;
		}

	}

	/**
	 * 
	 * SetOperationsImpl
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	public class SetOperationsImpl implements SetOperations, InvocationHandler {

		final SetOperations proxy;

		SetOperationsImpl() {
			this.proxy = (SetOperations) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { SetOperations.class }, this);
		}

		@Override
		public void add(String key, Object object) {
			set.add(key, object);
			retain(key, this);
		}

		@Override
		public void append(String key, Collection<Object> c) {
			set.addAll(key, c);
			retain(key, this);
		}

		@Override
		public Object pollFirst(String key) {
			return set.pollFirst(key);
		}

		@Override
		public Object pollLast(String key) {
			return set.pollLast(key);
		}

		@Override
		public Object peekFirst(String key) {
			return set.peekFirst(key);
		}

		@Override
		public Object peekLast(String key) {
			return set.peekLast(key);
		}

		@Override
		public List<Object> list(String key) {
			Set<Object> values;
			if ((values = set.get(key)) != null) {
				return new ArrayList<Object>(values);
			}
			return null;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
			Object result = MethodUtils.invokeMethod(this, method, arguments);
			if (!method.isAnnotationPresent(Sort.class)) {
				return result;
			}
			final String key = (String) arguments[0];
			keyExpirationPolicy.onSort(key, BasicCache.this);
			return result;
		}

		public SetOperations getProxy() {
			return proxy;
		}

	}

	/**
	 * 
	 * ListOperationsImpl
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	public class ListOperationsImpl implements ListOperations, InvocationHandler {

		final ListOperations proxy;

		ListOperationsImpl() {
			this.proxy = (ListOperations) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { ListOperations.class },
					this);
		}

		@Override
		public void addFirst(String key, Object object) {
			list.addFirst(key, object);
			retain(key, this);
		}

		@Override
		public void addLast(String key, Object object) {
			list.addLast(key, object);
			retain(key, this);
		}

		@Override
		public void append(String key, Collection<Object> c) {
			list.addAll(key, c);
			retain(key, this);
		}

		@Override
		public List<Object> list(String key) {
			Deque<Object> values;
			if ((values = list.get(key)) != null) {
				return new ArrayList<Object>(values);
			}
			return null;
		}

		@Override
		public Object peekFirst(String key) {
			return list.peekFirst(key);
		}

		@Override
		public Object peekLast(String key) {
			return list.peekLast(key);
		}

		@Override
		public Object pollFirst(String key) {
			return list.pollFirst(key);
		}

		@Override
		public Object pollLast(String key) {
			return list.pollLast(key);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
			Object result = MethodUtils.invokeMethod(this, method, arguments);
			if (!method.isAnnotationPresent(Sort.class)) {
				return result;
			}
			final String key = (String) arguments[0];
			keyExpirationPolicy.onSort(key, BasicCache.this);
			return result;
		}

		public ListOperations getProxy() {
			return proxy;
		}

	}

	class ValueOperationsImpl implements ValueOperations, InvocationHandler {

		final ValueOperations proxy;

		ValueOperationsImpl() {
			this.proxy = (ValueOperations) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { ValueOperations.class },
					this);
		}

		@Override
		public void set(String key, Object object) {
			cache.put(key, object);
			retain(key, this);
		}

		@Override
		public Object get(String key) {
			return cache.get(key);
		}

		@Override
		public boolean hasKey(String key) {
			return cache.containsKey(key) || longs.containsKey(key) || doubles.containsKey(key) || hash.containsKey(key)
					|| set.containsKey(key) || list.containsKey(key);
		}

		@Override
		public long longValue(String key) {
			return getLong(key).get();
		}

		@Override
		public long increment(String key) {
			return getLong(key).incrementAndGet();
		}

		@Override
		public long decrement(String key) {
			return getLong(key).decrementAndGet();
		}

		@Override
		public long addLong(String key, long delta) {
			return getLong(key).addAndGet(delta);
		}

		private AtomicLong getLong(String key) {
			AtomicLong l = MapUtils.get(longs, key, () -> {
				return new AtomicLong();
			});
			retain(key, this);
			return l;
		}

		@Override
		public double doubleValue(String key) {
			return getDouble(key).get();
		}

		@Override
		public double addDouble(String key, double delta) {
			return getDouble(key).addAndGet(delta);
		}

		private AtomicDouble getDouble(String key) {
			AtomicDouble d = MapUtils.get(doubles, key, () -> {
				return new AtomicDouble();
			});
			retain(key, this);
			return d;
		}

		@Override
		public void evict(String key, RemovalReason removalReason) {
			for (Map<String, ?> data : signatures.keySet()) {
				Object value;
				if ((value = data.remove(key)) != null) {
					removalListener.onRemoval(new RemovalNotification(key, value, removalReason));
				}
			}
		}

		@Override
		public int size() {
			int total = 0;
			for (Map<String, ?> data : signatures.keySet()) {
				total += data.size();
			}
			return total;
		}

		@Override
		public void clear() {
			for (Map<String, ?> data : signatures.keySet()) {
				data.clear();
			}
		}

		@Override
		public Set<String> keys() {
			Set<String> keys = new HashSet<String>();
			for (Map<String, ?> data : signatures.keySet()) {
				keys.addAll(data.keySet());
			}
			return keys;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object result = method.invoke(this, args);
			if (method.isAnnotationPresent(Sort.class)) {
				final String key = (String) args[0];
				keyExpirationPolicy.onSort(key, BasicCache.this);
			} else if (method.isAnnotationPresent(Delete.class)) {
				final String key = (String) args[0];
				keyExpirationPolicy.onDelete(key, BasicCache.this);
			} else if (method.isAnnotationPresent(Clear.class)) {
				keyExpirationPolicy.onClear(BasicCache.this);
			}
			return result;
		}

		public ValueOperations getProxy() {
			return proxy;
		}
	}

	/**
	 * Retain itself key and data, remove siblings' key and data
	 * 
	 * @param key
	 * @param signature
	 */
	private void retain(String key, Signature signature) {
		for (Map.Entry<Map<String, ?>, Signature> entry : signatures.entrySet()) {
			if (entry.getValue() != signature) {
				if (entry.getKey().size() > 0) {
					Object value;
					if ((value = entry.getKey().remove(key)) != null) {
						removalListener.onRemoval(new RemovalNotification(key, value, RemovalReason.REPLACEMENT));
					}
				}
			}
		}
	}

	@Override
	public void set(String key, Object object) {
		valueOperationsProxy.set(key, object);
	}

	@Override
	public boolean hasKey(String key) {
		return valueOperationsProxy.hasKey(key);
	}

	@Override
	public Object get(String key) {
		return valueOperationsProxy.get(key);
	}

	@Override
	public long longValue(String key) {
		return valueOperationsProxy.longValue(key);
	}

	@Override
	public long increment(String key) {
		return valueOperationsProxy.increment(key);
	}

	@Override
	public long decrement(String key) {
		return valueOperationsProxy.decrement(key);
	}

	@Override
	public long addLong(String key, long delta) {
		return valueOperationsProxy.addLong(key, delta);
	}

	@Override
	public double doubleValue(String key) {
		return valueOperationsProxy.doubleValue(key);
	}

	@Override
	public double addDouble(String key, double delta) {
		return valueOperationsProxy.addDouble(key, delta);
	}

	@Override
	public Set<String> keys() {
		return valueOperationsProxy.keys();
	}

	@Override
	public int size() {
		return valueOperationsProxy.size();
	}

	@Override
	public void evict(String key, RemovalReason removalReason) {
		valueOperationsProxy.evict(key, removalReason);
	}

	@Override
	public void clear() {
		valueOperationsProxy.clear();
	}

	@Override
	public HashOperations hash() {
		return hashOperationsProxy;
	}

	@Override
	public SetOperations set() {
		return setOperationsProxy;
	}

	@Override
	public ListOperations list() {
		return listOperationsProxy;
	}

}
