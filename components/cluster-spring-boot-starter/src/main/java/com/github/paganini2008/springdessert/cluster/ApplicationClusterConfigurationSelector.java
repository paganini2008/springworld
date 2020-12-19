package com.github.paganini2008.springdessert.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import com.github.paganini2008.springdessert.cluster.consistency.ConsistencyRequestConfig;
import com.github.paganini2008.springdessert.cluster.election.ApplicationClusterLeaderConfig;
import com.github.paganini2008.springdessert.cluster.http.RestClientConfig;
import com.github.paganini2008.springdessert.cluster.monitor.HealthIndicatorConfig;
import com.github.paganini2008.springdessert.cluster.multicast.ApplicationMulticastConfig;
import com.github.paganini2008.springdessert.cluster.pool.ProcessPoolConfig;
import com.github.paganini2008.springdessert.cluster.utils.ApplicationUtilityConfig;

/**
 * 
 * ApplicationClusterConfigurationSelector
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationClusterConfigurationSelector implements ImportSelector {

	private static final String[] relatedMulticastClassNames = new String[] { ApplicationMulticastConfig.class.getName(),
			ProcessPoolConfig.class.getName(), ConsistencyRequestConfig.class.getName(), RestClientConfig.class.getName() };

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		List<String> importedClassNames = new ArrayList<String>();
		importedClassNames.add(ApplicationUtilityConfig.class.getName());

		AnnotationAttributes annotationAttributes = AnnotationAttributes
				.fromMap(importingClassMetadata.getAnnotationAttributes(EnableApplicationCluster.class.getName()));
		if (annotationAttributes.getBoolean("multicast")) {
			importedClassNames.addAll(Arrays.asList(relatedMulticastClassNames));
		}
		if (annotationAttributes.getBoolean("leader")) {
			importedClassNames.add(ApplicationClusterLeaderConfig.class.getName());
		}
		if (annotationAttributes.getBoolean("monitor")) {
			importedClassNames.add(HealthIndicatorConfig.class.getName());
		}
		return importedClassNames.toArray(new String[0]);
	}
}
