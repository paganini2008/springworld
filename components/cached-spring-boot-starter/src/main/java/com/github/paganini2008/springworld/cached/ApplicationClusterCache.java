package com.github.paganini2008.springworld.cached;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.springworld.cluster.consistency.ConsistencyRequest;
import com.github.paganini2008.springworld.cluster.consistency.ConsistencyRequestConfirmationEvent;
import com.github.paganini2008.springworld.cluster.consistency.ConsistencyRequestContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationClusterCache
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ApplicationClusterCache implements Cache, ApplicationListener<ConsistencyRequestConfirmationEvent>, ApplicationContextAware {

	@Autowired
	private Cache cache;

	@Autowired
	private ConsistencyRequestContext context;
	
	private int timeout = 60;

	private ApplicationContext applicationContext;
	private final Observable operations = Observable.unrepeatable();
	private final Observable signatures = Observable.repeatable();
	private final Semaphore lock;
	private final ApplicationClusterHashOperations hashOperations =new ApplicationClusterHashOperations();
	private final  ApplicationClusterSetOperations setOperations = new ApplicationClusterSetOperations();
	private final ApplicationClusterListOperations listOperations = new ApplicationClusterListOperations();

	public ApplicationClusterCache(boolean serializable) {
		this.lock = serializable ? new Semaphore(1, true) : new Semaphore(Runtime.getRuntime().availableProcessors() * 2);
		configure();
	}

	@SuppressWarnings("unchecked")
	private void configure() {
		signatures.addObserver(MethodSignatures.parse("cache", "set"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.set(operationNotification.getKey(), operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("cache", "increment"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.increment(operationNotification.getKey());
		});
		signatures.addObserver(MethodSignatures.parse("cache", "decrement"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.decrement(operationNotification.getKey());
		});
		signatures.addObserver(MethodSignatures.parse("cache", "addLong"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.addLong(operationNotification.getKey(), (Long) operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("cache", "addDouble"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.addDouble(operationNotification.getKey(), (Double) operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("cache", "delete"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.delete(operationNotification.getKey());
		});
		signatures.addObserver(MethodSignatures.parse("cache", "clear"), (ob, argument) -> {
			cache.clear();
		});

		signatures.addObserver(MethodSignatures.parse("hash", "set"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.hash().set(operationNotification.getKey(), operationNotification.getName(), operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("hash", "append"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.hash().append(operationNotification.getKey(), (Map<String, Object>) operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("hash", "delete"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.hash().delete(operationNotification.getKey(), operationNotification.getName());
		});

		signatures.addObserver(MethodSignatures.parse("set", "add"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.set().add(operationNotification.getKey(), operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("set", "append"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.set().append(operationNotification.getKey(), (Collection<Object>) operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("set", "pollFirst"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.set().pollFirst(operationNotification.getKey());
		});
		signatures.addObserver(MethodSignatures.parse("set", "pollLast"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.set().pollLast(operationNotification.getKey());
		});

		signatures.addObserver(MethodSignatures.parse("list", "addFirst"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.list().addFirst(operationNotification.getKey(), operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("list", "addLast"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.list().addLast(operationNotification.getKey(), operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("list", "append"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.list().append(operationNotification.getKey(), (Collection<Object>) operationNotification.getValue());
		});
		signatures.addObserver(MethodSignatures.parse("list", "pollFirst"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.list().pollFirst(operationNotification.getKey());
		});
		signatures.addObserver(MethodSignatures.parse("list", "pollLast"), (ob, argument) -> {
			final OperationNotification operationNotification = (OperationNotification) argument;
			cache.list().pollLast(operationNotification.getKey());
		});
	}

	@Override
	public void set(String key, Object object) {
		OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("cache", "set"), key, object);
		processInBackground(key, operationNotification);
	}

	private void processInBackground(String key, Object object) {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
		if (context.propose(key, object, timeout)) {
			operations.addObserver(key, (ob, argument) -> {
				lock.release();
				applicationContext.publishEvent(new OperationNotificationEvent(this, (OperationNotification) argument));
			});
		} else {
			lock.release();
		}
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
		public Object get(String key, String name) {
			return cache.hash().get(key, name);
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
			Object result = peekFirst(key);
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("list", "pollFirst"), key);
			processInBackground(key, operationNotification);
			return result;
		}

		@Override
		public Object pollLast(String key) {
			Object result = peekLast(key);
			OperationNotification operationNotification = new OperationNotification(MethodSignatures.parse("list", "pollLast"), key);
			processInBackground(key, operationNotification);
			return result;
		}

		@Override
		public Object getProxy() {
			return cache.list().getProxy();
		}

	}

	@Override
	public void onApplicationEvent(ConsistencyRequestConfirmationEvent event) {
		final ConsistencyRequest result = (ConsistencyRequest) event.getSource();
		if (event.isOk()) {
			final String name = result.getName();
			final OperationNotification operationNotification = (OperationNotification) result.getValue();
			operations.notifyObservers(name, operationNotification);
			signatures.notifyObservers(operationNotification.getSignature(), operationNotification);
			if (log.isTraceEnabled()) {
				log.trace("ApplicationClusterCache size: " + size());
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
