package com.github.paganini2008.springdessert.cluster.gateway;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.github.paganini2008.devtools.collection.KeyMatchedMap;

/**
 * 
 * RouterManager
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public final class RouterManager extends KeyMatchedMap<String, Router> implements ApplicationListener<ContextRefreshedEvent> {

	private static final long serialVersionUID = -1981160524314626755L;

	public RouterManager() {
		super(new ConcurrentHashMap<String, Router>());
	}

	private final PathMatcher pathMatcher = new AntPathMatcher();

	public Router route(String prefix) {
		putIfAbsent(prefix, new Router(prefix));
		return match(prefix);
	}

	public Router match(String path) {
		return get(path);
	}

	@Override
	protected boolean apply(String pattern, Object inputKey) {
		return pathMatcher.match(pattern, (String) inputKey);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		
	}

}
