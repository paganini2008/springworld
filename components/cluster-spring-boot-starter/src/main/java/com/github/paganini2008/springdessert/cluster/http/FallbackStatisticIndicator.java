package com.github.paganini2008.springdessert.cluster.http;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.collection.MultiMappedMap;

/**
 * 
 * FallbackStatisticIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class FallbackStatisticIndicator implements RequestInterceptor, StatisticIndicator {

	private final MultiMappedMap<String, String, StatisticMetric> cache = new MultiMappedMap<String, String, StatisticMetric>();

	private Float timeoutPercentage = 0.8F;
	private Float errorPercentage = 0.8F;

	public void setTimeoutPercentage(Float timeoutPercentage) {
		this.timeoutPercentage = timeoutPercentage;
	}

	public void setErrorPercentage(Float errorPercentage) {
		this.errorPercentage = errorPercentage;
	}

	public Statistic getStatistic(String provider, String path) {
		return cache.get(provider, path);
	}

	@Override
	public boolean beforeSubmit(String provider, Request request) {
		boolean proceed = true;
		Statistic apiHealthMetrics = cache.get(provider, request.getPath(), () -> {
			return new StatisticMetric(provider, request.getPath());
		});
		if (timeoutPercentage != null) {
			long totalExecutionCount = apiHealthMetrics.getTotalExecutionCount();
			long timeoutExecutionCount = apiHealthMetrics.getTimeoutExecutionCount();
			proceed &= (float) (timeoutExecutionCount / totalExecutionCount) < timeoutPercentage;
		}
		if (errorPercentage != null) {
			long totalExecutionCount = apiHealthMetrics.getTotalExecutionCount();
			long failedExecutionCount = apiHealthMetrics.getFailedExecutionCount();
			proceed &= (float) (failedExecutionCount / totalExecutionCount) < errorPercentage;
		}
		return proceed;
	}

	@Override
	public void afterSubmit(String provider, Request request, ResponseEntity<?> responseEntity, Throwable e) {
		StatisticMetric apiHealthMetrics = cache.get(provider, request.getPath());
		apiHealthMetrics.getSnapshot().addRequest(request);
		apiHealthMetrics.getTotalExecution().incrementAndGet();
		if (responseEntity != null && (responseEntity.getStatusCodeValue() < 200 || responseEntity.getStatusCodeValue() >= 300)) {
			apiHealthMetrics.getFailedExecution().incrementAndGet();
		} else if (e != null && e instanceof RestClientException) {
			if (isRequestTimeout((RestClientException) e)) {
				apiHealthMetrics.getTimeoutExecution().incrementAndGet();
			} else {
				apiHealthMetrics.getFailedExecution().incrementAndGet();
			}
		}
	}

	private boolean isRequestTimeout(RestClientException e) {
		return e instanceof RestfulException && ((RestfulException) e).getInterruptedType() == InterruptedType.REQUEST_TIMEOUT;
	}

}
