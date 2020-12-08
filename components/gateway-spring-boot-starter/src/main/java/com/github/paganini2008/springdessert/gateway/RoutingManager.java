package com.github.paganini2008.springdessert.gateway;

/**
 * 
 * RoutingManager
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public final class RoutingManager {

	private final PathMatchedMap<Route> routingEntries = new PathMatchedMap<Route>();

	public Route route(String prefix) {
		routingEntries.putIfAbsent(prefix, new Route(prefix));
		return routingEntries.get(prefix);
	}

	public int countOfRouting() {
		return routingEntries.size();
	}

	public Route match(String path) {
		return routingEntries.get(path);
	}

}
