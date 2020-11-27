package com.github.paganini2008.springdessert.xmemcached;

import com.github.paganini2008.springdessert.xmemcached.serializer.KryoMemcachedSerializer;
import com.github.paganini2008.springdessert.xmemcached.serializer.MemcachedSerializer;
import com.google.code.yanf4j.core.impl.StandardSocketOption;

import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;

/**
 * 
 * MemcachedTemplateBuilder
 *
 * @author Fred Feng
 * @version 1.0
 */
public class MemcachedTemplateBuilder {

	private String address = "localhost:11211";
	private int connectionPoolSize = 8;
	private long sessionIdleTimeout = 10000;
	private int soTimeout = 60000;
	private MemcachedSerializer serializer;

	public MemcachedTemplateBuilder setAddress(String address) {
		this.address = address;
		return this;
	}

	public MemcachedTemplateBuilder setConnectionPoolSize(int connectionPoolSize) {
		this.connectionPoolSize = connectionPoolSize;
		return this;
	}

	public MemcachedTemplateBuilder setSessionIdleTimeout(long sessionIdleTimeout) {
		this.sessionIdleTimeout = sessionIdleTimeout;
		return this;
	}

	public MemcachedTemplateBuilder setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
		return this;
	}

	public MemcachedTemplateBuilder setSerializer(MemcachedSerializer serializer) {
		this.serializer = serializer;
		return this;
	}

	public MemcachedTemplate build() throws Exception {
		XMemcachedClientBuilder clientBuilder = new XMemcachedClientBuilder(AddrUtil.getAddresses(address));
		clientBuilder.setConnectionPoolSize(connectionPoolSize);
		clientBuilder.getConfiguration().setSessionIdleTimeout(sessionIdleTimeout);
		clientBuilder.getConfiguration().setSoTimeout(soTimeout);

		clientBuilder.setSocketOption(StandardSocketOption.SO_RCVBUF, 64 * 1024);
		clientBuilder.setSocketOption(StandardSocketOption.SO_SNDBUF, 32 * 1024);
		clientBuilder.setSocketOption(StandardSocketOption.TCP_NODELAY, false);

		clientBuilder.setFailureMode(true);
		clientBuilder.setCommandFactory(new BinaryCommandFactory());
		if (serializer == null) {
			serializer = new KryoMemcachedSerializer();
		}
		return new MemcachedTemplate(clientBuilder.build(), serializer);
	}

}
