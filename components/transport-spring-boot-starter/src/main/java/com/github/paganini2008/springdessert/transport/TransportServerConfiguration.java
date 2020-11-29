package com.github.paganini2008.springdessert.transport;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.glassfish.grizzly.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springdessert.transport.buffer.BufferZone;
import com.github.paganini2008.springdessert.transport.buffer.KafkaBufferZone;
import com.github.paganini2008.springdessert.transport.buffer.MemcachedBufferZone;
import com.github.paganini2008.springdessert.transport.buffer.RedisBufferZone;
import com.github.paganini2008.springdessert.transport.transport.EmbeddedChannelEventListener;
import com.github.paganini2008.springdessert.transport.transport.EmbeddedServer;
import com.github.paganini2008.springdessert.transport.transport.EmbeddedServerHandler;
import com.github.paganini2008.springdessert.transport.transport.GrizzlyChannelEventListener;
import com.github.paganini2008.springdessert.transport.transport.GrizzlyServer;
import com.github.paganini2008.springdessert.transport.transport.GrizzlyServerHandler;
import com.github.paganini2008.springdessert.transport.transport.MinaChannelEventListener;
import com.github.paganini2008.springdessert.transport.transport.MinaServer;
import com.github.paganini2008.springdessert.transport.transport.MinaServerHandler;
import com.github.paganini2008.springdessert.transport.transport.NettyChannelEventListener;
import com.github.paganini2008.springdessert.transport.transport.NettyServer;
import com.github.paganini2008.springdessert.transport.transport.NettyServerHandler;
import com.github.paganini2008.springdessert.transport.transport.NettyServerKeepAlivePolicy;
import com.github.paganini2008.springdessert.transport.transport.NioServer;
import com.github.paganini2008.springdessert.xmemcached.MemcachedTemplate;
import com.github.paganini2008.springdessert.xmemcached.MemcachedTemplateBuilder;
import com.github.paganini2008.springdessert.xmemcached.serializer.FstMemcachedSerializer;
import com.github.paganini2008.springdessert.xmemcached.serializer.MemcachedSerializer;
import com.github.paganini2008.transport.ChannelEventListener;
import com.github.paganini2008.transport.NioClient;
import com.github.paganini2008.transport.Partitioner;
import com.github.paganini2008.transport.RoundRobinPartitioner;
import com.github.paganini2008.transport.TransportNodeCentre;
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
import com.github.paganini2008.transport.serializer.FstSerializer;
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

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@ConditionalOnMissingBean
	@Bean
	public Serializer serializer() {
		return new FstSerializer();
	}

	@Bean(destroyMethod = "stop")
	public TupleLoopProcessor tupleLoopProcessor() {
		return new TupleLoopProcessor();
	}

	@Bean
	public Partitioner partitioner() {
		return new RoundRobinPartitioner();
	}

	@Bean
	public ScaleoutClusterListener scaleoutClusterListener() {
		return new ScaleoutClusterListener();
	}

	@Bean
	public TransportNodeCentre transportNodeCentre() {
		return new DefaultTransportNodeCentre();
	}

	@Bean
	public ProcessLogging processLogging() {
		return new ProcessLogging();
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public Counter counter(RedisConnectionFactory redisConnectionFactory) {
		final String name = String.format("spring:application:cluster:%s:transport:counter", clusterName);
		return new Counter(name, redisConnectionFactory);
	}

	@ConditionalOnProperty(name = "spring.application.transport.bufferzone", havingValue = "redis", matchIfMissing = true)
	@Bean
	public BufferZone redisBufferZone() {
		return new RedisBufferZone();
	}

	@ConditionalOnProperty(name = "spring.application.transport.bufferzone", havingValue = "kafka")
	@Bean
	public BufferZone kafkaBufferZone() {
		return new KafkaBufferZone();

	}

	/**
	 * 
	 * MemcachedBufferZoneConfiguration
	 * 
	 * @author Fred Feng
	 *
	 * @since 1.0
	 */
	@Configuration
	@ConditionalOnProperty(name = "spring.application.transport.bufferzone", havingValue = "memcached")
	public static class MemcachedBufferZoneConfiguration {

		@Value("${spring.memcached.address:localhost:11211}")
		private String address;

		@ConditionalOnMissingBean(MemcachedTemplate.class)
		@Bean
		public MemcachedTemplate memcachedTemplate(MemcachedSerializer memcachedSerializer) throws Exception {
			MemcachedTemplateBuilder builder = new MemcachedTemplateBuilder();
			builder.setAddress(address);
			builder.setSerializer(new FstMemcachedSerializer());
			return builder.build();
		}

		@Bean
		public BufferZone bufferZone() {
			return new MemcachedBufferZone();
		}
	}

	@Configuration
	@ConditionalOnProperty(name = "spring.application.transport.nioserver", havingValue = "netty", matchIfMissing = true)
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
	@ConditionalOnProperty(name = "spring.application.transport.nioserver", havingValue = "mina")
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
	@ConditionalOnProperty(name = "spring.application.transport.nioserver", havingValue = "grizzly")
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
	@ConditionalOnProperty(name = "spring.application.transport.nioserver", havingValue = "embedded-io")
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
