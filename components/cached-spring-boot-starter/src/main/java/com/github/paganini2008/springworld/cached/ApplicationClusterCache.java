package com.github.paganini2008.springworld.cached;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.springworld.cached.base.Cache;
import com.github.paganini2008.springworld.cached.base.RemovalReason;

/**
 * 
 * ApplicationClusterCache
 *
 * @author Fred Feng
 * @since 1.0
 */
public class ApplicationClusterCache implements Cache, ApplicationContextAware {

	@Qualifier("cacheDelegate")
	@Autowired
	private Cache cache;

	private final ApplicationClusterHashOperations hashOperations = new ApplicationClusterHashOperations();
	private final ApplicationClusterSetOperations setOperations = new ApplicationClusterSetOperations();
	private final ApplicationClusterListOperations listOperations = new ApplicationClusterListOperations();

	@Override
	public void set(String key, Object object) {
		OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("cache", "set"), key, object);
		processInBackground(key, operationNotification);
	}

	private void processInBackground(String key, OperationNotification operationNotification) {
		applicationContext.publishEvent(new OperationNotificationEvent(this, operationNotification));
	}

	@Override
	public boolean hasKey(String key) {
		return cache.hasKey(key);
	}

	@Override
	public Object get(String key) {
		return cache.get(key);
	}

	@Override
	public long longValue(String key) {
		return cache.longValue(key);
	}

	@Override
	public long increment(String key) {
		OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("cache", "increment"), key);
		processInBackground(key, operationNotification);
		return -1;
	}

	@Override
	public long decrement(String key) {
		OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("cache", "decrement"), key);
		processInBackground(key, operationNotification);
		return -1;
	}

	@Override
	public long addLong(String key, long delta) {
		OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("cache", "addLong"), key, delta);
		processInBackground(key, operationNotification);
		return -1;
	}

	@Override
	public double doubleValue(String key) {
		return cache.doubleValue(key);
	}

	@Override
	public double addDouble(String key, double delta) {
		OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("cache", "addDouble"), key, delta);
		processInBackground(key, operationNotification);
		return -1;
	}

	@Override
	public Set<String> keys() {
		return cache.keys();
	}

	@Override
	public int size() {
		return cache.size();
	}

	@Override
	public void evict(String key, RemovalReason removalReason) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String key) {
		OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("cache", "delete"), key);
		processInBackground(key, operationNotification);
	}

	@Override
	public void clear() {
		OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("cache", "clear"), null);
		processInBackground("clear", operationNotification);
	}

	@Override
	public HashOperations hash() {
		return hashOperations;
	}

	@Override
	public SetOperations set() {
		return setOperations;
	}

	@Override
	public ListOperations list() {
		return listOperations;
	}

	/**
	 * 
	 * ApplicationClusterHashOperations
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	public class ApplicationClusterHashOperations implements HashOperations {

		ApplicationClusterHashOperations() {
		}

		@Override
		public void set(String key, String name, Object object) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("hash", "set"), key, name,
					object);
			processInBackground(key, operationNotification);
		}

		@Override
		public void append(String key, Map<String, Object> m) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("hash", "append"), key, m);
			processInBackground(key, operationNotification);
		}

		@Override
		public boolean hasKey(String key, String name) {
			return cache.hash().hasKey(key, name);
		}

		@Override
		public Object get(String key, String name, Object defaultValue) {
			return cache.hash().get(key, name, defaultValue);
		}

		@Override
		public Map<String, Object> get(String key) {
			return cache.hash().get(key);
		}

		@Override
		public void delete(String key, String name) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("hash", "delete"), key, name);
			processInBackground(key, operationNotification);
		}

		@Override
		public Object getProxy() {
			return cache.hash().getProxy();
		}

	}

	/**
	 * 
	 * ApplicationClusterSetOperations
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	public class ApplicationClusterSetOperations implements SetOperations {

		ApplicationClusterSetOperations() {
		}

		@Override
		public void add(String key, Object object) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("set", "add"), key, object);
			processInBackground(key, operationNotification);
		}

		@Override
		public void append(String key, Collection<Object> c) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("set", "append"), key, c);
			processInBackground(key, operationNotification);
		}

		@Override
		public Object pollFirst(String key) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("set", "pollFirst"), key);
			processInBackground(key, operationNotification);
			return null;
		}

		@Override
		public Object pollLast(String key) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("set", "pollLast"), key);
			processInBackground(key, operationNotification);
			return null;
		}

		@Override
		public Object peekFirst(String key) {
			return cache.set().peekFirst(key);
		}

		@Override
		public Object peekLast(String key) {
			return cache.set().peekLast(key);
		}

		@Override
		public List<Object> list(String key) {
			return cache.set().list(key);
		}

		@Override
		public Object getProxy() {
			return cache.set().getProxy();
		}

	}

	/**
	 * 
	 * ApplicationClusterListOperations
	 *
	 * @author Fred Feng
	 * @since 1.0
	 */
	public class ApplicationClusterListOperations implements ListOperations {

		ApplicationClusterListOperations() {
		}

		@Override
		public void addFirst(String key, Object object) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("list", "addFirst"), key,
					object);
			processInBackground(key, operationNotification);
		}

		@Override
		public void addLast(String key, Object object) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("list", "addLast"), key, object);
			processInBackground(key, operationNotification);
		}

		@Override
		public void append(String key, Collection<Object> c) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("list", "append"), key, c);
			processInBackground(key, operationNotification);
		}

		@Override
		public List<Object> list(String key) {
			return cache.list().list(key);
		}

		@Override
		public Object peekFirst(String key) {
			return cache.list().peekFirst(key);
		}

		@Override
		public Object peekLast(String key) {
			return cache.list().peekLast(key);
		}

		@Override
		public Object pollFirst(String key) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("list", "pollFirst"), key);
			processInBackground(key, operationNotification);
			return null;
		}

		@Override
		public Object pollLast(String key) {
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("list", "pollLast"), key);
			processInBackground(key, operationNotification);
			return null;
		}

		@Override
		public Object getProxy() {
			return cache.list().getProxy();
		}

	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
