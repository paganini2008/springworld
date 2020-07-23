package com.github.paganini2008.springworld.redisplus.messager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.github.paganini2008.springworld.redisplus.BeanNames;
import com.github.paganini2008.springworld.redisplus.common.TtlKeeper;

/**
 * 
 * RedisMessagerConfig
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisMessagerConfig {

	@ConditionalOnMissingBean(RedisSerializer.class)
	@Bean(BeanNames.REDIS_SERIALIZER)
	public RedisSerializer<Object> redisSerializer() {
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
		ObjectMapper om = new ObjectMapper();
		om.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		return jackson2JsonRedisSerializer;
	}

	@Bean(BeanNames.REDIS_TEMPLATE)
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory,
			@Qualifier(BeanNames.REDIS_SERIALIZER) RedisSerializer<Object> redisSerializer) {
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

	@Bean
	@ConditionalOnMissingBean(StringRedisTemplate.class)
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new StringRedisTemplate(redisConnectionFactory);
	}

	@Bean
	public KeyExpirationEventMessageListener keyExpirationEventMessageListener(
			@Qualifier(BeanNames.REDIS_MESSAGE_LISTENER_CONTAINER) RedisMessageListenerContainer redisMessageListenerContainer) {
		KeyExpirationEventMessageListener listener = new KeyExpirationEventMessageListener(redisMessageListenerContainer);
		listener.setKeyspaceNotificationsConfigParameter("Ex");
		return listener;
	}

	@Bean(BeanNames.REDIS_MESSAGE_EVENT_PUBLISHER)
	public RedisMessageEventPublisher redisMessageEventPublisher() {
		return new RedisMessageEventPublisher();
	}

	@ConditionalOnProperty(name = "spring.redis.messager.dispatcher.mode", havingValue = "pubsub", matchIfMissing = true)
	@Configuration
	public static class PubSubModeConfig {

		@Value("${spring.redis.messager.pubsub.channel:messager-pubsub}")
		private String pubsubChannelKey;

		@Bean(BeanNames.PUBSUB_REDIS_MESSAGE_DISPATCHER)
		public RedisMessageDispatcher pubSubRedisMessageDispatcher() {
			return new PubSubRedisMessageDispatcher();
		}

		@Bean(BeanNames.REDIS_MESSAGE_PUBSUB_LISTENER)
		public MessageListenerAdapter redisMessagePubsubListener(
				@Qualifier(BeanNames.REDIS_MESSAGE_EVENT_PUBLISHER) RedisMessageEventPublisher eventPublisher,
				@Qualifier(BeanNames.REDIS_SERIALIZER) RedisSerializer<Object> redisSerializer) {
			MessageListenerAdapter adapter = new MessageListenerAdapter(eventPublisher, "doPubsub");
			adapter.setSerializer(redisSerializer);
			adapter.afterPropertiesSet();
			return adapter;
		}

		@Bean(BeanNames.REDIS_MESSAGE_LISTENER_CONTAINER)
		public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
				@Qualifier(BeanNames.REDIS_MESSAGE_PUBSUB_LISTENER) MessageListenerAdapter redisMessagePubsubListener) {
			RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
			redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
			redisMessageListenerContainer.addMessageListener(redisMessagePubsubListener, new ChannelTopic(pubsubChannelKey));
			return redisMessageListenerContainer;
		}

		@Bean
		public PubSubRedisKeyExpiredEventListener pubSubRedisKeyExpiredEventListener() {
			return new PubSubRedisKeyExpiredEventListener();
		}
	}

	@ConditionalOnProperty(name = "spring.redis.messager.dispatcher.mode", havingValue = "queue")
	@Configuration
	public static class QueueModeConfig {

		@Value("${spring.redis.messager.queue.channel:messager-queue}")
		private String queueChannelKey;

		@ConditionalOnProperty(name = "spring.redis.messager.dispatcher.mode", havingValue = "queue")
		@Bean(BeanNames.QUEUE_REDIS_MESSAGE_DISPATCHER)
		public RedisMessageDispatcher queueRedisMessageDispatcher() {
			return new QueueRedisMessageDispatcher();
		}

		@Bean(BeanNames.REDIS_MESSAGE_QUEUE_LISTENER)
		public MessageListenerAdapter redisMessageQueueListener(
				@Qualifier(BeanNames.REDIS_MESSAGE_EVENT_PUBLISHER) RedisMessageEventPublisher eventPublisher,
				@Qualifier(BeanNames.REDIS_SERIALIZER) RedisSerializer<Object> redisSerializer) {
			MessageListenerAdapter adapter = new MessageListenerAdapter(eventPublisher, "doQueue");
			adapter.setSerializer(redisSerializer);
			adapter.afterPropertiesSet();
			return adapter;
		}

		@Bean(BeanNames.REDIS_MESSAGE_LISTENER_CONTAINER)
		public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
				@Qualifier(BeanNames.REDIS_MESSAGE_QUEUE_LISTENER) MessageListenerAdapter redisMessageQueueListener) {
			RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
			redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
			redisMessageListenerContainer.addMessageListener(redisMessageQueueListener, new ChannelTopic(queueChannelKey));
			return redisMessageListenerContainer;
		}

		@Bean
		public QueueRedisKeyExpiredEventListener queueRedisKeyExpiredEventListener() {
			return new QueueRedisKeyExpiredEventListener();
		}
	}

	@Bean(BeanNames.REDIS_MESSAGE_EVENT_LISTENER)
	public RedisMessageEventListener redisMessageEventListener() {
		return new RedisMessageEventListener();
	}

	@Bean(BeanNames.REDIS_MESSAGE_SENDER)
	public RedisMessageSender redisMessageSender() {
		return new RedisMessageSender();
	}

	@Bean
	public RedisMessageHandlerBeanProcessor redisMessageHandlerBeanProcessor() {
		return new RedisMessageHandlerBeanProcessor();
	}

	@Bean(destroyMethod = "stop")
	public TtlKeeper ttlKeeper(@Qualifier(BeanNames.REDIS_TEMPLATE) RedisTemplate<String, Object> redisTemplate) {
		return new TtlKeeper(redisTemplate);
	}

}
