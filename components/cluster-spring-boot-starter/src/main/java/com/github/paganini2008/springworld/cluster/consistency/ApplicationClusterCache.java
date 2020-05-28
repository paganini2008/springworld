package com.github.paganini2008.springworld.cluster.consistency;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	private final Lock lock = new ReentrantLock();
	private final Cache delegate;
	private int timeout = 60;

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
		this(new HashCache());
	}

	public ApplicationClusterCache(Cache delegate) {
		this.delegate = delegate;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public void putObject(Object key, Object value) {
		putObject(key, value, null);
	}

	public void putObject(final Object key, final Object value, Watcher watcher) {
		lock.lock();
		final String name = key.toString();
		observable.addObserver(name, (ob, arg) -> {
			lock.unlock();
			Object expectedValue = getObject((String) arg);
			if (ObjectUtils.equals(expectedValue, value)) {
				if (watcher != null) {
					watcher.process(name);
				}
			} else {
				putObject(key, value, watcher);
			}
		});
		context.propose(name, value, timeout);
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
		final String name = result.getName();
		delegate.putObject(name, result.getValue());
		observable.notifyObservers(name, name);
		if (log.isTraceEnabled()) {
			log.trace("Current cache'size: " + getSize());
		}
	}

}
