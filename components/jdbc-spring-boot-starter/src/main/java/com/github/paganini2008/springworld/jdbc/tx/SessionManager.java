package com.github.paganini2008.springworld.jdbc.tx;

import org.springframework.beans.factory.annotation.Autowired;

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

	private final ThreadLocal<Session> threadLocal = new ThreadLocal<Session>() {

		@Override
		protected Session initialValue() {
			return new NoTransactionSession(sqlPlus);
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

}
