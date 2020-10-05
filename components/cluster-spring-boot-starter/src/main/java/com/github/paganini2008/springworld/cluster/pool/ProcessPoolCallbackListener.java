package com.github.paganini2008.springworld.cluster.pool;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ProcessPoolCallbackListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ProcessPoolCallbackListener implements ClusterMessageListener {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		final Signature signature = (Signature) message;
		final Object bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
		if (bean != null) {
			try {
				MethodUtils.invokeMethod(bean, signature.getMethodName(), signature.getArguments());
				redisMessageSender.sendMessage(signature.getId(), message);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			log.warn("No bean registered in spring context to call the method of signature: " + signature);
		}
	}

	@Override
	public String getTopic() {
		return ProcessPoolCallbackListener.class.getName();
	}

}
