package com.github.paganini2008.springworld.xa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 
 * XaAutoConfiguration
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
@Import({ ApplicationContextUtils.class })
public class XaAutoConfiguration {

	@Bean
	public XaTransactionManager xaTransactionManager() {
		return new DefaultXaTransactionManager();
	}
	
	@Bean
	public XaTransactionManagerProcessor xaTransactionManagerProcessor() {
		return new XaTransactionManagerProcessor();
	}
	
}
