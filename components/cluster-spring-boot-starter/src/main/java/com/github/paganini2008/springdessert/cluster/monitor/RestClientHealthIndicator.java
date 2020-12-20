package com.github.paganini2008.springdessert.cluster.monitor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
		Map<String, List<Statistic>> source = statisticIndicator.toMap();
		if (MapUtils.isNotEmpty(source)) {
			for (Map.Entry<String, List<Statistic>> entry : source.entrySet()) {
				builder.withDetail(entry.getKey(), entry.getValue().stream().map(stat -> stat.toMap()).collect(Collectors.toList()));
			}
		}
	}

}
