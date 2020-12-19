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
	private Float permitPercentage = 0.8F;

	public void setTimeoutPercentage(Float timeoutPercentage) {
		this.timeoutPercentage = timeoutPercentage;
	}

	public void setErrorPercentage(Float errorPercentage) {
		this.errorPercentage = errorPercentage;
	}

	public void setPermitPercentage(Float permitPercentage) {
		this.permitPercentage = permitPercentage;
	}

	public Statistic getStatistic(String provider, String path) {
		return cache.get(provider, path);
	}

	@Override
	public boolean beforeSubmit(String provider, Request request) {
		boolean proceed = true;
		Statistic statistic = cache.get(provider, request.getPath(), () -> {
			return new StatisticMetric(provider, request.getPath(), ((ForwardedRequest) request).getAllowedPermits());
		});
		if (timeoutPercentage != null) {
			long totalExecutionCount = statistic.getTotalExecutionCount();
			long timeoutExecutionCount = statistic.getTimeoutExecutionCount();
			proceed &= (float) (timeoutExecutionCount / totalExecutionCount) < timeoutPercentage.floatValue();
		}
		if (errorPercentage != null) {
			long totalExecutionCount = statistic.getTotalExecutionCount();
			long failedExecutionCount = statistic.getFailedExecutionCount();
			proceed &= (float) (failedExecutionCount / totalExecutionCount) < errorPercentage.floatValue();
		}
		if (permitPercentage != null) {
			long maxPermits = statistic.getPermit().maxPermits();
			long availablePermits = statistic.getPermit().availablePermits();
			proceed &= (float) (maxPermits - availablePermits / maxPermits) < permitPercentage.floatValue();
		}
		return proceed;
	}

	@Override
	public void afterSubmit(String provider, Request request, ResponseEntity<?> responseEntity, Throwable e) {
		StatisticMetric statistic = cache.get(provider, request.getPath());
		statistic.getSnapshot().addRequest(request);
		statistic.getTotalExecution().incrementAndGet();
		if (responseEntity != null && (responseEntity.getStatusCodeValue() < 200 || responseEntity.getStatusCodeValue() >= 300)) {
			statistic.getFailedExecution().incrementAndGet();
		} else if (e != null && e instanceof RestClientException) {
			if (isRequestTimeout((RestClientException) e)) {
				statistic.getTimeoutExecution().incrementAndGet();
			} else {
				statistic.getFailedExecution().incrementAndGet();
			}
		}
	}

	private boolean isRequestTimeout(RestClientException e) {
		return e instanceof RestfulException && ((RestfulException) e).getInterruptedType() == InterruptedType.REQUEST_TIMEOUT;
	}

}
