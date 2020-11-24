package com.github.paganini2008.springdessert.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.springdessert.db4j.Db4jClassPathDaoScanner;
import com.github.paganini2008.springdessert.jdbc.annotations.DaoScan;

/**
 * 
 * DaoScannerRegistrar
 *
 * @author Fred Feng
 * @since 1.0
 */
public class DaoScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

	private static final boolean db4jPresent;

	static {
		db4jPresent = ClassUtils.isPresent("com.github.paganini2008.devtools.db4j.SqlRunner");
	}

	private ResourceLoader resourceLoader;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		AnnotationAttributes annotationAttributes = AnnotationAttributes
				.fromMap(importingClassMetadata.getAnnotationAttributes(DaoScan.class.getName()));
		List<String> basePackages = new ArrayList<String>();
		if (annotationAttributes.containsKey("basePackages")) {
			for (String basePackage : annotationAttributes.getStringArray("basePackages")) {
				if (StringUtils.hasText(basePackage)) {
					basePackages.add(basePackage);
				}
			}
		}
		ClassPathBeanDefinitionScanner scanner = db4jPresent ? new Db4jClassPathDaoScanner(registry) : new ClassPathDaoScanner(registry);
		if (resourceLoader != null) {
			scanner.setResourceLoader(resourceLoader);
		}
		scanner.scan(StringUtils.toStringArray(basePackages));
	}

}
