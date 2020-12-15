package com.github.paganini2008.springdessert.cluster.gateway;

/**
 * 
 * RoutingManager
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public final class RoutingManager {

	private final PathMatchedMap<Router> routingEntries = new PathMatchedMap<Router>();

	public Router route(String prefix) {
		routingEntries.putIfAbsent(prefix, new Router(prefix));
		return routingEntries.get(prefix);
	}

	public int countOfRouting() {
		return routingEntries.size();
	}

	public Router match(String path) {
		return routingEntries.get(path);
	}

}
