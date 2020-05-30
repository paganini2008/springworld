package com.github.paganini2008.springworld.cluster.consistency;

import java.util.Set;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.ObjectUtils;
import com.github.paganini2008.devtools.Observable;
import com.github.paganini2008.devtools.cache.AbstractCache;
import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.cache.HashCache;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationClusterCache
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ApplicationClusterCache extends AbstractCache implements ApplicationListener<ConsistencyRequestConfirmationEvent> {

	@Autowired
	private ConsistencyRequestContext context;
	private final Observable observable = Observable.unrepeatable();
	private final Cache delegate;
	private int timeout = 60;
	private final Semaphore lock;

	/**
	 * 
	 * Watcher
	 *
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	public static interface Watcher {

		void process(String name);

	}

	public ApplicationClusterCache() {
		this(new HashCache(), false);
	}

	public ApplicationClusterCache(Cache delegate, boolean serializable) {
		this.delegate = delegate;
		this.lock = serializable ? new Semaphore(1, true) : new Semaphore(Runtime.getRuntime().availableProcessors() * 2);
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public void putObject(Object key, Object value) {
		putObject(key, value, null);
	}

	public void putObject(final Object key, final Object value, Watcher watcher) {
		final String name = key.toString();
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
		if (context.propose(name, value, timeout)) {
			observable.addObserver(name, (ob, arg) -> {
				lock.release();
				Object expectedValue = getObject((String) arg);
				if (ObjectUtils.equals(expectedValue, value)) {
					if (watcher != null) {
						watcher.process(name);
					}
				} else {
					putObject(key, value, watcher);
				}
			});
		} else {
			lock.release();
		}
	}

	@Override
	public boolean hasKey(Object key) {
		return delegate.hasKey(key);
	}

	@Override
	public Object getObject(Object key) {
		return delegate.getObject(key);
	}

	@Override
	public Object removeObject(Object key) {
		return delegate.removeObject(key);
	}

	@Override
	public Set<Object> keys() {
		return delegate.keys();
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	@Override
	public void close() {
		observable.clearObservers();
		clear();
		delegate.close();
	}

	@Override
	public void onApplicationEvent(ConsistencyRequestConfirmationEvent event) {
		final ConsistencyRequest result = (ConsistencyRequest) event.getSource();
		if (event.isOk()) {
			final String name = result.getName();
			delegate.putObject(name, result.getValue());
			observable.notifyObservers(name, name);
			if (log.isTraceEnabled()) {
				log.trace("Current cache'size: " + getSize());
			}
		}
	}

}
