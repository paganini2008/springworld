package com.github.paganini2008.springdessert.xtransport;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.glassfish.grizzly.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springdessert.xmemcached.MemcachedTemplate;
import com.github.paganini2008.springdessert.xmemcached.MemcachedTemplateBuilder;
import com.github.paganini2008.springdessert.xmemcached.serializer.FstMemcachedSerializer;
import com.github.paganini2008.springdessert.xmemcached.serializer.MemcachedSerializer;
import com.github.paganini2008.springdessert.xtransport.buffer.BufferZone;
import com.github.paganini2008.springdessert.xtransport.buffer.MemcachedBufferZone;
import com.github.paganini2008.springdessert.xtransport.buffer.RedisBufferZone;
import com.github.paganini2008.springdessert.xtransport.transport.EmbeddedChannelEventListener;
import com.github.paganini2008.springdessert.xtransport.transport.EmbeddedServer;
import com.github.paganini2008.springdessert.xtransport.transport.EmbeddedServerHandler;
import com.github.paganini2008.springdessert.xtransport.transport.GrizzlyChannelEventListener;
import com.github.paganini2008.springdessert.xtransport.transport.GrizzlyServer;
import com.github.paganini2008.springdessert.xtransport.transport.GrizzlyServerHandler;
import com.github.paganini2008.springdessert.xtransport.transport.MinaChannelEventListener;
import com.github.paganini2008.springdessert.xtransport.transport.MinaServer;
import com.github.paganini2008.springdessert.xtransport.transport.MinaServerHandler;
import com.github.paganini2008.springdessert.xtransport.transport.NettyChannelEventListener;
import com.github.paganini2008.springdessert.xtransport.transport.NettyServer;
import com.github.paganini2008.springdessert.xtransport.transport.NettyServerHandler;
import com.github.paganini2008.springdessert.xtransport.transport.NettyServerKeepAlivePolicy;
import com.github.paganini2008.springdessert.xtransport.transport.NioServer;
import com.github.paganini2008.xtransport.ChannelEventListener;
import com.github.paganini2008.xtransport.NioClient;
import com.github.paganini2008.xtransport.Partitioner;
import com.github.paganini2008.xtransport.RoundRobinPartitioner;
import com.github.paganini2008.xtransport.TransportNodeCentre;
import com.github.paganini2008.xtransport.embeddedio.EmbeddedClient;
import com.github.paganini2008.xtransport.embeddedio.EmbeddedSerializationFactory;
import com.github.paganini2008.xtransport.embeddedio.SerializationFactory;
import com.github.paganini2008.xtransport.grizzly.GrizzlyClient;
import com.github.paganini2008.xtransport.grizzly.GrizzlyTupleCodecFactory;
import com.github.paganini2008.xtransport.grizzly.TupleCodecFactory;
import com.github.paganini2008.xtransport.mina.MinaClient;
import com.github.paganini2008.xtransport.mina.MinaTupleCodecFactory;
import com.github.paganini2008.xtransport.netty.KeepAlivePolicy;
import com.github.paganini2008.xtransport.netty.MessageCodecFactory;
import com.github.paganini2008.xtransport.netty.NettyClient;
import com.github.paganini2008.xtransport.netty.NettyTupleCodecFactory;
import com.github.paganini2008.xtransport.serializer.FstSerializer;
import com.github.paganini2008.xtransport.serializer.Serializer;

import io.netty.channel.Channel;

/**
 * 
 * XtransportAutoConfiguration
 * 
 * @author Jimmy Hoff
 * @version 1.0
 */
@Import({ ApplicationTransportController.class, BenchmarkController.class })
@Configuration
public class XtransportAutoConfiguration {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@ConditionalOnMissingBean
	@Bean
	public Serializer serializer() {
		return new FstSerializer();
	}

	@Bean
	public TupleLoopProcessor tupleLoopProcessor() {
		return new TupleLoopProcessor();
	}

	@ConditionalOnMissingBean
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

	@Bean(name = "consumer", initMethod = "start", destroyMethod = "stop")
	public Counter consumer(RedisConnectionFactory redisConnectionFactory) {
		final String name = String.format("spring:application:cluster:%s:transport:counter:consumer", clusterName);
		return new Counter(name, redisConnectionFactory);
	}

	@Bean(name = "producer", initMethod = "start", destroyMethod = "stop")
	public Counter producer(RedisConnectionFactory redisConnectionFactory) {
		final String name = String.format("spring:application:cluster:%s:transport:counter:producer", clusterName);
		return new Counter(name, redisConnectionFactory);
	}

	@ConditionalOnMissingBean
	@Bean
	public BufferZone redisBufferZone() {
		return new RedisBufferZone();
	}

	/**
	 * 
	 * MemcachedBufferZoneConfiguration
	 * 
	 * @author Jimmy Hoff
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
