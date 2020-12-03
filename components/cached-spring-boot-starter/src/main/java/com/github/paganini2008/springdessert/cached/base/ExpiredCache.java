package com.github.paganini2008.springdessert.cached.base;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 
 * ExpiredCache
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public interface ExpiredCache extends Cache {

	void expire(String key, long time, TimeUnit timeUnit);

	void expireAt(String key, Date deadline);

}
