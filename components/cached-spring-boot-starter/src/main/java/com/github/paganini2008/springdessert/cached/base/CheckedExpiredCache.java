package com.github.paganini2008.springdessert.cached.base;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;

/**
 * 
 * CheckedExpiredCache
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class CheckedExpiredCache implements ExpiredCache, Executable {

	private final Map<String, long[]> expirationWatcher = new ConcurrentHashMap<String, long[]>();
	private final Cache delegate;
	private Timer timer;

	public CheckedExpiredCache(Cache delegate) {
		this.delegate = delegate;
		this.timer = ThreadUtils.scheduleAtFixedRate(this, 1, TimeUnit.SECONDS);
	}

	@Override
	public void set(String key, Object object) {
		delegate.set(key, object);
	}

	@Override
	public boolean hasKey(String key) {
		return delegate.hasKey(key);
	}

	@Override
	public Object get(String key) {
		return delegate.get(key);
	}

	@Override
	public long longValue(String key) {
		return delegate.longValue(key);
	}

	@Override
	public long increment(String key) {
		return delegate.increment(key);
	}

	@Override
	public long decrement(String key) {
		return delegate.decrement(key);
	}

	@Override
	public long addLong(String key, long delta) {
		return delegate.addLong(key, delta);
	}

	@Override
	public double doubleValue(String key) {
		return delegate.doubleValue(key);
	}

	@Override
	public double addDouble(String key, double delta) {
		return delegate.addDouble(key, delta);
	}

	@Override
	public Set<String> keys() {
		return delegate.keys();
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public void delete(String key) {
		delegate.delete(key);
	}

	@Override
	public void evict(String key, RemovalReason removalReason) {
		delegate.evict(key, removalReason);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public HashOperations hash() {
		return delegate.hash();
	}

	@Override
	public SetOperations set() {
		return delegate.set();
	}

	@Override
	public ListOperations list() {
		return delegate.list();
	}

	@Override
	public void expire(String key, long time, TimeUnit timeUnit) {
		final long now = System.currentTimeMillis();
		expirationWatcher.put(key, new long[] { now, now + DateUtils.convertToMillis(time, timeUnit) });
	}

	@Override
	public void expireAt(String key, Date deadline) {
		final long now = System.currentTimeMillis();
		if (now >= deadline.getTime()) {
			throw new IllegalArgumentException("Deadline date is past");
		}
		expirationWatcher.put(key, new long[] { now, deadline.getTime() });
	}

	@Override
	public boolean execute() {
		if (expirationWatcher.isEmpty()) {
			return true;
		}
		long now = System.currentTimeMillis();
		String key;
		long[] times;
		long createdTime, expiredTime;
		for (Map.Entry<String, long[]> entry : expirationWatcher.entrySet()) {
			key = entry.getKey();
			times = entry.getValue();
			createdTime = times[0];
			expiredTime = times[1];
			if (now - createdTime >= expiredTime) {
				delegate.delete(key);
				expirationWatcher.remove(key);
			}
		}
		return true;
	}

	@Override
	public void close() {
		if (timer != null) {
			timer.cancel();
		}
	}

}
