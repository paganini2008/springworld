package com.github.paganini2008.springworld.cluster.pool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.redisplus.common.SharedLatch;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ProcessPoolTaskListener
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class ProcessPoolTaskListener implements ClusterMessageListener {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private PendingQueue pendingQueue;

	@Autowired
	private ProcessPool processPool;

	@Autowired
	private SharedLatch sharedLatch;

	@Autowired
	private InvocationResult invocationResult;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, Object message) {
		Object bean = null;
		Object result = null;
		Signature signature = null;
		try {
			signature = (Signature) message;
			bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
			if (bean != null) {
				invocationResult.setCompleted();
				result = MethodUtils.invokeMethod(bean, signature.getMethodName(), signature.getArguments());
				MethodUtils.invokeMethodWithAnnotation(bean, OnSuccess.class, signature, result);
			} else {
				log.warn("No bean registered in spring context to call the signature: " + signature);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			MethodUtils.invokeMethodWithAnnotation(bean, OnFailure.class, signature, e);
		} finally {
			sharedLatch.release();

			signature = pendingQueue.get();
			if (signature != null) {
				processPool.submit(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()), signature.getMethodName(),
						signature.getArguments());
			}
		}
	}

	@Override
	public String getTopic() {
		return ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + ":" + applicationName + ":process-pool-task";
	}

}
