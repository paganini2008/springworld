package com.github.paganini2008.springworld.jobsoup;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 
 * DeployModeConfigurationSelector
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class DeployModeConfigurationSelector implements ImportSelector {

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		AnnotationAttributes annotationAttributes = AnnotationAttributes
				.fromMap(importingClassMetadata.getAnnotationAttributes(EnableJobSoupApi.class.getName()));
		DeployMode deployMode = annotationAttributes.getEnum("value");
		return new String[] { deployMode.getConfigurationClassName() };
	}

}
