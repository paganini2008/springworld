package com.github.paganini2008.springdessert.tx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.cache.LruCache;
import com.github.paganini2008.devtools.db4j.SqlPlus;

/**
 * 
 * SessionManager
 *
 * @author Fred Feng
 * @version 1.0
 */
public final class SessionManager {

	@Autowired
	private SqlPlus sqlPlus;

	@Qualifier("global-cache")
	@Autowired(required = false)
	private Cache cache;

	@Value("${spring.application.jdbc.tx.useCache:false}")
	private boolean useCache;

	@Value("${spring.application.jdbc.tx.cacheSize:128}")
	private int cacheSize;

	private final ThreadLocal<Session> threadLocal = new ThreadLocal<Session>() {

		@Override
		protected Session initialValue() {
			return new NotTransactionalSession(sqlPlus, allocateCache());
		}

	};

	public Session current() {
		return threadLocal.get();
	}

	public void set(Session session) {
		threadLocal.set(session);
	}

	public void reset() {
		threadLocal.remove();
	}

	public void bindTransaction(JdbcTransaction transaction, long timeout) {
		set(new TransactionalSession(transaction, allocateCache(), timeout));
	}

	private Cache allocateCache() {
		return useCache ? new LruCache(cacheSize) : cache;
	}

}
