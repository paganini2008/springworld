package com.github.paganini2008.springworld.jobsoup;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

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
public class DeployModeConfigurationSelector implements ImportSelector {

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		AnnotationAttributes annotationAttributes = AnnotationAttributes
				.fromMap(importingClassMetadata.getAnnotationAttributes(EnableJobSoupApi.class.getName()));
		DeployMode deployMode = annotationAttributes.getEnum("value");
		Banner.printBanner(deployMode.name().toLowerCase(), log);
		return new String[] { deployMode.getConfigurationClassName() };
	}

}
