package com.github.paganini2008.springworld.xa;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.redis.pubsub.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaTransactionContext
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class XaTransactionContext extends ThreadLocal<XaTransactionContext> {

	private static final XaTransactionContext instance = new XaTransactionContext();

	XaTransactionContext() {
	}

	private final Map<String, XaTransactionResponse> results = new HashMap<String, XaTransactionResponse>();
	private final AtomicBoolean ok = new AtomicBoolean(true);
	private CountDownLatch latch;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Value("${spring.application.xa.responseTimeout:60}")
	private int timeout;

	@Override
	protected final XaTransactionContext initialValue() {
		return ApplicationContextUtils.autowireBean(new XaTransactionContext());
	}

	public void setOk(boolean value) {
		ok.set(value);
	}

	public void await(XaTransaction transaction) {
		int count = redisTemplate.hasKey(transaction.getXaId()) ? redisTemplate.opsForList().size(transaction.getXaId()).intValue() : 0;
		if (count == 0) {
			return;
		}
		latch = new CountDownLatch(count);
		String nextId = redisTemplate.opsForList().leftPop(transaction.getXaId());
		if (StringUtils.isNotBlank(nextId)) {
			redisMessageSender.sendMessage("confirmation:" + nextId, ok.get());
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void complete(XaTransactionResponse response) {
		results.put(response.getId(), response);
		if (ok.get()) {
			ok.set(response.isOk());
		}
		log.info(response.toString());
		latch.countDown();

		String nextId = redisTemplate.opsForList().leftPop(response.getXaId());
		if (StringUtils.isNotBlank(nextId)) {
			redisMessageSender.sendMessage("confirmation:" + nextId, ok.get());
		} else {
			redisTemplate.delete(response.getXaId());
		}
	}

	public boolean isOk() {
		return ok.get();
	}

	public XaTransactionResponse[] getReponses() {
		return results.values().toArray(new XaTransactionResponse[0]);
	}

	public void destroy() {
		remove();
	}

	public static XaTransactionContext current() {
		return instance.get();
	}

}
