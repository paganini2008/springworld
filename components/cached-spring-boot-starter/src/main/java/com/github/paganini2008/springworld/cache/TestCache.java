package com.github.paganini2008.springworld.cache;

public class TestCache {

	public static void main(String[] args) {
		Cache cache = new SortedCache(20, true);
		for (int i = 0; i < 100; i++) {
			cache.set("a:" + i, i);
		}
		System.out.println(cache.keys());
		System.out.println(cache.size());
		System.out.println(cache.get("a:1"));
		System.out.println(cache.get("a:5"));
		System.out.println(cache.get("a:9"));
		cache.hash().set("a", "n1", "v1");
		cache.hash().set("a", "n2", "v1");
		System.out.println(cache.hash().get("a"));
		System.out.println(cache.hasKey("a"));
		System.out.println(cache.addLong("a", 1000001));
		System.out.println(cache.longValue("a"));
		System.out.println(cache.hash().get("a"));
	}

}
