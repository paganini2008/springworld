package com.github.paganini2008.springworld.tx.openfeign;

import static com.github.paganini2008.springworld.tx.XaTransactionManager.XA_HTTP_REQUEST_IDENTITY;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.paganini2008.devtools.StringUtils;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * FeignConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@Configuration
@ConditionalOnClass(FeignAutoConfiguration.class)
public class OpenFeignConfig {

	@ConditionalOnMissingBean(HystrixConcurrencyStrategy.class)
	@Bean("requestAttributes")
	public HystrixConcurrencyStrategy requestAttributesHystrixConcurrencyStrategy() {
		return new RequestAttributesHystrixConcurrencyStrategy();
	}

	@Bean
	public RequestInterceptor xaRequestInterceptor() {
		return new XaRequestInterceptor();
	}

	/**
	 * 
	 * XaRequestInterceptor
	 *
	 * @author Fred Feng
	 * @version 1.0
	 */
	public static class XaRequestInterceptor implements RequestInterceptor {

		@Override
		public void apply(RequestTemplate template) {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			if (request != null) {
				String xaId = (String) request.getAttribute(XA_HTTP_REQUEST_IDENTITY);
				if (StringUtils.isNotBlank(xaId)) {
					if (log.isTraceEnabled()) {
						log.trace("Servlet path '{}' associates with XA Transaction '{}'.", template.url(), xaId);
					}
					template.header(XA_HTTP_REQUEST_IDENTITY, xaId);
				}
			} else {
				log.warn("Can not get HttpServletRequest from RequestContextHolder for running XA Request, please check it.");
			}
		}

	}

}
