package com.github.paganini2008.springdessert.cluster.monitor;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.cluster.ApplicationClusterContext;
import com.github.paganini2008.springdessert.cluster.HealthState;
import com.github.paganini2008.springdessert.cluster.http.Statistic;
import com.github.paganini2008.springdessert.cluster.http.StatisticIndicator;

/**
 * 
 * HttpStatisticHealthIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class HttpStatisticHealthIndicator extends AbstractHealthIndicator {

	@Qualifier("requestStatistic")
	@Autowired
	private StatisticIndicator requestStatistic;

	@Qualifier("responseStatistic")
	@Autowired
	private StatisticIndicator responseStatistic;

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
		Map<String, Collection<Statistic>> source = requestStatistic.toMap();
		if (MapUtils.isNotEmpty(source)) {
			for (Map.Entry<String, Collection<Statistic>> entry : source.entrySet()) {
				builder.withDetail("[-]: " + entry.getKey(),
						entry.getValue().stream().map(stat -> stat.toMap()).collect(Collectors.toList()));
			}
		}
		source = responseStatistic.toMap();
		if (MapUtils.isNotEmpty(source)) {
			for (Map.Entry<String, Collection<Statistic>> entry : source.entrySet()) {
				builder.withDetail("[+]: " + entry.getKey(),
						entry.getValue().stream().map(stat -> stat.toMap()).collect(Collectors.toList()));
			}
		}

	}

}
