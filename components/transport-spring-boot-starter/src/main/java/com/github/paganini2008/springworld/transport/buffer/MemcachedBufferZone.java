package com.github.paganini2008.springworld.transport.buffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.cluster.ClusterId;
import com.github.paganini2008.transport.Tuple;
import com.github.paganini2008.transport.serializer.Serializer;
import com.google.code.yanf4j.core.impl.StandardSocketOption;

import lombok.extern.slf4j.Slf4j;
import net.rubyeye.xmemcached.Counter;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;

/**
 * 
 * MemcachedBufferZone
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class MemcachedBufferZone implements BufferZone {

	private static final String PUSHING_KEY = ":pushing";
	private static final String POPPING_KEY = ":popping";

	@Value("${spring.transport.memcached.address:localhost:11211}")
	private String address;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.transport.bufferzone.collectionName:default}")
	private String collectionName;

	@Value("${spring.transport.bufferzone.cooperative:true}")
	private boolean cooperative;

	@Autowired
	private ClusterId clusterId;

	@Autowired
	private Serializer serializer;

	private MemcachedClient client;

	private String rootKey;
	private Counter pushing;
	private Counter popping;

	@Override
	public void configure() throws Exception {
		XMemcachedClientBuilder clientBuilder = new XMemcachedClientBuilder(AddrUtil.getAddresses(address));
		clientBuilder.setConnectionPoolSize(8);
		clientBuilder.setSocketOption(StandardSocketOption.SO_RCVBUF, 64 * 1024);
		clientBuilder.setSocketOption(StandardSocketOption.SO_SNDBUF, 32 * 1024);
		clientBuilder.setSocketOption(StandardSocketOption.TCP_NODELAY, false);
		clientBuilder.getConfiguration().setSessionIdleTimeout(10000);
		configureClient(clientBuilder);
		client = clientBuilder.build();

		rootKey = getKey(collectionName);
		pushing = client.getCounter(rootKey + PUSHING_KEY, 0);
		popping = client.getCounter(rootKey + POPPING_KEY, 0);
	}

	protected void configureClient(XMemcachedClientBuilder clientBuilder) {
	}

	@Override
	public void destroy() {
		try {
			client.shutdown();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void set(String name, Tuple tuple) throws Exception {
		if (popping.get() > pushing.get()) {
			pushing.set(popping.get());
		}
		String key = rootKey + ":" + pushing.incrementAndGet();
		client.set(key, 0, serializer.serialize(tuple));
	}

	@Override
	public Tuple get(String name) throws Exception {
		if (pushing.get() <= popping.get()) {
			return null;
		}
		String key = rootKey + ":" + popping.incrementAndGet();
		byte[] bytes;
		if ((bytes = (byte[]) client.get(key)) != null) {
			client.deleteWithNoReply(key);
			return (Tuple) serializer.deserialize(bytes);
		}
		return null;
	}

	@Override
	public int size(String name) throws Exception {
		Map<InetSocketAddress, Map<String, String>> result = client.getStats();
		int total = 0;
		if (result != null) {
			for (Map<String, String> map : result.values()) {
				total += map.containsKey("curr_items") ? Integer.parseInt(map.get("curr_items")) : 0;
			}
		}
		return total;
	}

	private String getKey(String name) {
		return "transport:bufferzone:" + name + ":" + applicationName + (cooperative ? "" : ":" + clusterId.get());
	}

}
