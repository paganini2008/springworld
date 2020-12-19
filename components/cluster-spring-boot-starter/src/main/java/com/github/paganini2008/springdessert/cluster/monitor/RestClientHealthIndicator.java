package com.github.paganini2008.springdessert.cluster.monitor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterContext;
import com.github.paganini2008.springdessert.cluster.HealthState;
import com.github.paganini2008.springdessert.cluster.http.Statistic;
import com.github.paganini2008.springdessert.cluster.http.StatisticIndicator;

/**
 * 
 * RestClientHealthIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class RestClientHealthIndicator extends AbstractHealthIndicator {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private StatisticIndicator statisticIndicator;

	@Autowired
	private ApplicationClusterContext applicationClusterContext;

	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		HealthState healthState = applicationClusterContext.getHealthState();
		if (healthState == HealthState.FATAL) {
			builder.down();
		} else {
			builder.up();
		}
		Map<String, Statistic> source = statisticIndicator.getAll(applicationName);
		if (MapUtils.isNotEmpty(source)) {
			for (Map.Entry<String, Statistic> entry : source.entrySet()) {
				builder.withDetail(entry.getKey(), entry.getValue().toMap());
			}
		}
	}

}
