package com.github.paganini2008.springworld.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
import com.github.paganini2008.springworld.redis.pubsub.RedisEphemeralMessageListener;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageEventPublisher;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageHandlerBeanProcessor;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageListener;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

/**
 * 
 * RedisPubSubConfig
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisPubSubConfig {

	@Value("${spring.redis.pubsub.channel:pubsub}")
	private String channel;

	@Bean(BeanNames.REDIS_SERIALIZER)
	public RedisSerializer<Object> redisSerializer() {
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
		ObjectMapper om = new ObjectMapper();
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

	@DependsOn(BeanNames.REDIS_MESSAGE_EVENT_PUBLISHER)
	@Bean(BeanNames.MESSAGE_LISTENER_ADAPTER)
	public MessageListenerAdapter messageListenerAdapter(@Qualifier(BeanNames.REDIS_SERIALIZER) RedisSerializer<Object> redisSerializer) {
		MessageListenerAdapter adapter = new MessageListenerAdapter(redisMessageEventPublisher(), "publish");
		adapter.setSerializer(redisSerializer);
		adapter.afterPropertiesSet();
		return adapter;
	}

	@Bean(BeanNames.REDIS_MESSAGE_LISTENER_CONTAINER)
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
			@Qualifier(BeanNames.MESSAGE_LISTENER_ADAPTER) MessageListenerAdapter messageListenerAdapter) {
		RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
		redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
		redisMessageListenerContainer.addMessageListener(messageListenerAdapter, new ChannelTopic(channel));
		return redisMessageListenerContainer;
	}

	@Bean(BeanNames.REDIS_MESSAGE_LISTENER)
	public RedisMessageListener redisMessageListener() {
		return new RedisMessageListener();
	}

	@Bean(BeanNames.REDIS_EPHEMERAL_MESSAGE_LISTENER)
	public RedisEphemeralMessageListener redisEphemeralMessageListener() {
		return new RedisEphemeralMessageListener();
	}

	@Bean(BeanNames.REDIS_MESSAGE_SENDER)
	public RedisMessageSender redisMessageSender() {
		return new RedisMessageSender();
	}

	@Bean
	public RedisMessageHandlerBeanProcessor redisMessageHandlerBeanProcessor() {
		return new RedisMessageHandlerBeanProcessor();
	}

}
