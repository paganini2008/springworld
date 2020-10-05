package com.github.paganini2008.springworld.cluster.pool;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMessageListener;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;
import com.github.paganini2008.springworld.cluster.utils.ApplicationContextUtils;
import com.github.paganini2008.springworld.reditools.common.SharedLatch;

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
	private InvocationBarrier invocationBarrier;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		final Signature signature = (Signature) message;
		final Object bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
		if (bean != null) {
			try {
				invocationBarrier.setCompleted();
				Object result = MethodUtils.invokeMethod(bean, signature.getMethodName(), signature.getArguments());
				clusterMulticastGroup.unicast(applicationName, ProcessPoolCallbackListener.class.getName(), message);
				List<Method> methods = MethodUtils.getMethodsWithAnnotation(bean.getClass(), OnSuccess.class);
				for (Method method : methods) {
					clusterMulticastGroup.unicast(applicationName, ProcessPoolCallbackListener.class.getName(),
							new SuccessCallback(method.getName(), result, signature));
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				List<Method> methods = MethodUtils.getMethodsWithAnnotation(bean.getClass(), OnFailure.class);
				for (Method method : methods) {
					clusterMulticastGroup.unicast(applicationName, ProcessPoolCallbackListener.class.getName(),
							new FailureCallback(method.getName(), e, signature));
				}
			} finally {
				sharedLatch.release();

				Signature nextSignature = pendingQueue.get();
				if (nextSignature != null) {
					processPool.execute(nextSignature.getBeanName(), ClassUtils.forName(nextSignature.getBeanClassName()),
							nextSignature.getMethodName(), nextSignature.getArguments());
				}
			}
		} else {
			log.warn("No bean registered in spring context to call the method of signature: " + signature);
		}
	}

	@Override
	public String getTopic() {
		return ProcessPoolTaskListener.class.getName();
	}

}
