package com.github.paganini2008.springworld.config.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.springworld.config.BeanPropertyChangeEvent;
import com.github.paganini2008.springworld.config.ResultEntity;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConfigTestController
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/config/test")
public class ConfigTestController implements ApplicationListener<BeanPropertyChangeEvent> {

	@ToString
	@Component("test-1")
	public static class InnerBean {

		@Value("@{example.intValue:1}")
		private int intValue;

		@Value("@{example.floatValue:100}")
		private float floatValue;

		@Value("@{example.stringValue:Hello}")
		private String stringValue;

		@Value("@{example.booleanValue}")
		private boolean booleanValue;
	}

	@Autowired
	@Qualifier("test-1")
	private InnerBean test1;

	@GetMapping("/test1")
	public ResultEntity<InnerBean> test1() {
		log.info(test1.toString());
		return ResultEntity.onSuccess(test1);
	}

	@Override
	public void onApplicationEvent(BeanPropertyChangeEvent event) {
		log.info(event.toString());
	}

}
