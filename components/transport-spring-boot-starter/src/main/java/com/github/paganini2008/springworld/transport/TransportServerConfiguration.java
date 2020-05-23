package com.github.paganini2008.springworld.transport;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.glassfish.grizzly.Connection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.springworld.redis.KryoRedisSerializer;
import com.github.paganini2008.springworld.transport.buffer.BufferZone;
import com.github.paganini2008.springworld.transport.buffer.MemcachedBufferZone;
import com.github.paganini2008.springworld.transport.buffer.RedisBufferZone;
import com.github.paganini2008.springworld.transport.transport.EmbeddedChannelEventListener;
import com.github.paganini2008.springworld.transport.transport.EmbeddedServer;
import com.github.paganini2008.springworld.transport.transport.EmbeddedServerHandler;
import com.github.paganini2008.springworld.transport.transport.GrizzlyChannelEventListener;
import com.github.paganini2008.springworld.transport.transport.GrizzlyServer;
import com.github.paganini2008.springworld.transport.transport.GrizzlyServerHandler;
import com.github.paganini2008.springworld.transport.transport.MinaChannelEventListener;
import com.github.paganini2008.springworld.transport.transport.MinaServer;
import com.github.paganini2008.springworld.transport.transport.MinaServerHandler;
import com.github.paganini2008.springworld.transport.transport.NettyChannelEventListener;
import com.github.paganini2008.springworld.transport.transport.NettyServer;
import com.github.paganini2008.springworld.transport.transport.NettyServerHandler;
import com.github.paganini2008.springworld.transport.transport.NettyServerKeepAlivePolicy;
import com.github.paganini2008.springworld.transport.transport.NioServer;
import com.github.paganini2008.springworld.xmemcached.KryoMemcachedSerializer;
import com.github.paganini2008.springworld.xmemcached.MemcachedSerializer;
import com.github.paganini2008.springworld.xmemcached.MemcachedTemplate;
import com.github.paganini2008.springworld.xmemcached.MemcachedTemplateBuilder;
import com.github.paganini2008.transport.ChannelEventListener;
import com.github.paganini2008.transport.NioClient;
import com.github.paganini2008.transport.NodeFinder;
import com.github.paganini2008.transport.Partitioner;
import com.github.paganini2008.transport.RoundRobinPartitioner;
import com.github.paganini2008.transport.TupleImpl;
import com.github.paganini2008.transport.embeddedio.EmbeddedClient;
import com.github.paganini2008.transport.embeddedio.EmbeddedSerializationFactory;
import com.github.paganini2008.transport.embeddedio.SerializationFactory;
import com.github.paganini2008.transport.grizzly.GrizzlyClient;
import com.github.paganini2008.transport.grizzly.GrizzlyTupleCodecFactory;
import com.github.paganini2008.transport.grizzly.TupleCodecFactory;
import com.github.paganini2008.transport.mina.MinaClient;
import com.github.paganini2008.transport.mina.MinaTupleCodecFactory;
import com.github.paganini2008.transport.netty.KeepAlivePolicy;
import com.github.paganini2008.transport.netty.MessageCodecFactory;
import com.github.paganini2008.transport.netty.NettyClient;
import com.github.paganini2008.transport.netty.NettyTupleCodecFactory;
import com.github.paganini2008.transport.serializer.KryoSerializer;
import com.github.paganini2008.transport.serializer.Serializer;

import io.netty.channel.Channel;

/**
 * 
 * TransportServerConfiguration
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
public class TransportServerConfiguration {

	@ConditionalOnMissingBean(Serializer.class)
	@Bean
	public Serializer serializer() {
		return new KryoSerializer();
	}

	@Bean(destroyMethod = "stop")
	public LoopProcessor loopProcessor() {
		return new LoopProcessor();
	}

	@ConditionalOnMissingBean(Partitioner.class)
	@Bean
	public Partitioner partitioner() {
		return new RoundRobinPartitioner();
	}

	@Bean
	public NioServerPeerFinder nioServerPeerFinder() {
		return new NioServerPeerFinder();
	}

	@Bean(destroyMethod = "destroy")
	public NodeFinder contextNodeFinder() {
		return new ContextNodeFinder();
	}

	@Primary
	@Bean
	public ContextInitializer contextInitializer() {
		return new ContextInitializer();
	}

	@Bean
	public HandlerBeanPostProcessor handlerBeanPostProcessor() {
		return new HandlerBeanPostProcessor();
	}

	@Bean("redis-template-bigint")
	public RedisTemplate<String, Long> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Long> redisTemplate = new RedisTemplate<String, Long>();
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(new GenericToStringSerializer<Long>(Long.class));
		redisTemplate.setExposeConnection(true);
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	@Bean("redis-counter-bigint")
	public RedisAtomicLong redisAtomicLong(@Qualifier("redis-template-bigint") RedisTemplate<String, Long> redisTemplate) {
		return new RedisAtomicLong("transport:counter", redisTemplate);
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public Counter counter(@Qualifier("redis-counter-bigint") RedisAtomicLong redisAtomicLong) {
		return new Counter(redisAtomicLong);
	}

	@Configuration
	@ConditionalOnProperty(name = "spring.transport.bufferzone", havingValue = "redis", matchIfMissing = true)
	public static class RedisBufferZoneConfiguration {

		@Bean("bufferzone-redis-serializer")
		public RedisSerializer<Object> redisSerializer() {
			return new KryoRedisSerializer(TupleImpl.class);
		}

		@Bean("bufferzone-redis-template")
		public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory,
				@Qualifier("bufferzone-redis-serializer") RedisSerializer<Object> redisSerializer) {
			RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
			redisTemplate.setConnectionFactory(redisConnectionFactory);
			StringRedisSerializer stringSerializer = new StringRedisSerializer();
			redisTemplate.setKeySerializer(stringSerializer);
			redisTemplate.setValueSerializer(redisSerializer);
			redisTemplate.setHashKeySerializer(stringSerializer);
			redisTemplate.setHashValueSerializer(redisSerializer);
			redisTemplate.afterPropertiesSet();
			return redisTemplate;
		}

		@Bean(initMethod = "configure", destroyMethod = "destroy")
		public BufferZone bufferZone() {
			return new RedisBufferZone();
		}
	}

	@Configuration
	@ConditionalOnProperty(name = "spring.transport.bufferzone", havingValue = "memcached")
	public static class MemcachedBufferZoneConfiguration {

		@Value("${spring.memcached.address:localhost:11211}")
		private String address;

		@ConditionalOnMissingBean(MemcachedSerializer.class)
		@Bean
		public MemcachedSerializer memcachedSerializer() {
			return new KryoMemcachedSerializer();
		}

		@ConditionalOnMissingBean(MemcachedTemplate.class)
		@Bean
		public MemcachedTemplate memcachedTemplate(MemcachedSerializer memcachedSerializer) throws Exception {
			MemcachedTemplateBuilder builder = new MemcachedTemplateBuilder();
			builder.setAddress(address);
			builder.setSerializer(memcachedSerializer);
			return builder.build();
		}

		@Bean(initMethod = "configure", destroyMethod = "destroy")
		public BufferZone bufferZone() {
			return new MemcachedBufferZone();
		}
	}

	@Configuration
	@ConditionalOnProperty(name = "spring.transport.nioserver", havingValue = "netty", matchIfMissing = true)
	public static class NettyTransportConfiguration {

		@Bean(initMethod = "open", destroyMethod = "close")
		public NioClient nioClient(MessageCodecFactory codecFactory) {
			NettyClient nioClient = new NettyClient();
			nioClient.setMessageCodecFactory(codecFactory);
			return nioClient;
		}

		@Bean(initMethod = "start", destroyMethod = "stop")
		public NioServer nioServer() {
			return new NettyServer();
		}

		@ConditionalOnMissingBean(KeepAlivePolicy.class)
		@Bean
		public KeepAlivePolicy idlePolicy() {
			return new NettyServerKeepAlivePolicy();
		}

		@ConditionalOnMissingBean(MessageCodecFactory.class)
		@Bean
		public MessageCodecFactory codecFactory(Serializer serializer) {
			return new NettyTupleCodecFactory(serializer);
		}

		@Bean
		public NettyServerHandler serverHandler() {
			return new NettyServerHandler();
		}

		@ConditionalOnMissingBean(ChannelEventListener.class)
		@Bean
		public ChannelEventListener<Channel> channelEventListener() {
			return new NettyChannelEventListener();
		}
	}

	@Configuration
	@ConditionalOnProperty(name = "spring.transport.nioserver", havingValue = "mina")
	public static class MinaTransportConfiguration {

		@Bean(initMethod = "open", destroyMethod = "close")
		public NioClient nioClient(ProtocolCodecFactory codecFactory) {
			MinaClient nioClient = new MinaClient();
			nioClient.setProtocolCodecFactory(codecFactory);
			return nioClient;
		}

		@Bean(initMethod = "start", destroyMethod = "stop")
		public NioServer nioServer() {
			return new MinaServer();
		}

		@ConditionalOnMissingBean(ProtocolCodecFactory.class)
		@Bean
		public ProtocolCodecFactory codecFactory(Serializer serializer) {
			return new MinaTupleCodecFactory(serializer);
		}

		@Bean
		public MinaServerHandler serverHandler() {
			return new MinaServerHandler();
		}

		@ConditionalOnMissingBean(ChannelEventListener.class)
		@Bean
		public ChannelEventListener<IoSession> channelEventListener() {
			return new MinaChannelEventListener();
		}
	}

	@Configuration
	@ConditionalOnProperty(name = "spring.transport.nioserver", havingValue = "grizzly")
	public static class GrizzlyTransportConfiguration {

		@Bean(initMethod = "open", destroyMethod = "close")
		public NioClient nioClient(TupleCodecFactory codecFactory) {
			GrizzlyClient nioClient = new GrizzlyClient();
			nioClient.setTupleCodecFactory(codecFactory);
			return nioClient;
		}

		@Bean(initMethod = "start", destroyMethod = "stop")
		public NioServer nioServer() {
			return new GrizzlyServer();
		}

		@ConditionalOnMissingBean(TupleCodecFactory.class)
		@Bean
		public TupleCodecFactory codecFactory(Serializer serializer) {
			return new GrizzlyTupleCodecFactory(serializer);
		}

		@Bean
		public GrizzlyServerHandler serverHandler() {
			return new GrizzlyServerHandler();
		}

		@ConditionalOnMissingBean(ChannelEventListener.class)
		@Bean
		public ChannelEventListener<Connection<?>> channelEventListener() {
			return new GrizzlyChannelEventListener();
		}
	}

	@Configuration
	@ConditionalOnProperty(name = "spring.transport.nioserver", havingValue = "embedded-io")
	public static class EmbeddedIOTransportConfiguration {

		@Bean(initMethod = "open", destroyMethod = "close")
		public NioClient nioClient(SerializationFactory serializationFactory) {
			EmbeddedClient nioClient = new EmbeddedClient();
			nioClient.setSerializationFactory(serializationFactory);
			return nioClient;
		}

		@Bean(initMethod = "start", destroyMethod = "stop")
		public NioServer nioServer() {
			return new EmbeddedServer();
		}

		@ConditionalOnMissingBean(SerializationFactory.class)
		@Bean
		public SerializationFactory serializationFactory(Serializer serializer) {
			return new EmbeddedSerializationFactory(serializer);
		}

		@Bean
		public EmbeddedServerHandler serverHandler() {
			return new EmbeddedServerHandler();
		}

		@ConditionalOnMissingBean(ChannelEventListener.class)
		@Bean
		public ChannelEventListener<com.github.paganini2008.embeddedio.Channel> channelEventListener() {
			return new EmbeddedChannelEventListener();
		}
	}

}
