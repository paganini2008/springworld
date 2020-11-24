package com.github.paganini2008.springdessert.jobsoup;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.type.AnnotationMetadata;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springdessert.jobsoup.server.ServerMode;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DeployModeConfigurationSelector
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class DeployModeConfigurationSelector implements ImportSelector, EnvironmentAware {

	private ConfigurableEnvironment environment;

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		AnnotationAttributes annotationAttributes = AnnotationAttributes
				.fromMap(importingClassMetadata.getAnnotationAttributes(EnableJobSoupApi.class.getName()));
		DeployMode deployMode = annotationAttributes.getEnum("value");
		ServerMode serverMode = annotationAttributes.getEnum("serverMode");
		Map<String, Object> jobsoupConfig = new HashMap<String, Object>();
		if (deployMode == DeployMode.SERVER) {
			jobsoupConfig.put("jobsoup.server.mode.side", serverMode.getValue());
		}
		if (serverMode == ServerMode.CONSUMER) {
			String producerLocation = annotationAttributes.getString("producer");
			if (StringUtils.isNotBlank(producerLocation)) {
				jobsoupConfig.put("jobsoup.server.mode.producer.location", producerLocation);
			}
		}
		if (jobsoupConfig.size() > 0) {
			environment.getPropertySources().addLast(new MapPropertySource("jobsoupConfig", jobsoupConfig));
		}
		Banner.printBanner(deployMode.name().toLowerCase(), log);
		return new String[] { deployMode.getConfigurationClassName() };
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = (ConfigurableEnvironment) environment;
	}

}
