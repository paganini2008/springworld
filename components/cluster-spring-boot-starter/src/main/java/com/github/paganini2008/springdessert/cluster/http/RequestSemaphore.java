package com.github.paganini2008.springdessert.cluster.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.collection.MultiMappedMap;

/**
 * 
 * RequestSemaphore
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class RequestSemaphore {

	private final MultiMappedMap<String, String, Permit> apiPermitMap = new MultiMappedMap<String, String, Permit>();
	private final Map<String, Permit> providerPermitMap = new ConcurrentHashMap<String, Permit>();

	public Permit getPermit(String provider, int providerPermits, String path, int pathPermits) {
		return getPermit(provider, providerPermits, path, pathPermits, Integer.MAX_VALUE);
	}

	public Permit getPermit(String provider, int providerPermits, String path, int pathPermits, int defaultPermits) {
		final Permit providerPermit = MapUtils.get(providerPermitMap, provider, () -> {
			return new Permit(new Permit(defaultPermits), providerPermits);
		});
		return apiPermitMap.get(provider, path, () -> {
			return new Permit(providerPermit, pathPermits);
		});
	}

	public static class Permit {

		private final Permit parent;
		private final AtomicInteger counter;
		private final int maxPermits;

		Permit(int maxPermits) {
			this(null, maxPermits);
		}

		Permit(Permit parent, int maxPermits) {
			this.parent = parent;
			this.counter = new AtomicInteger(0);
			this.maxPermits = maxPermits;
		}

		public int accquire() {
			if (parent != null) {
				parent.accquire();
			}
			return counter.incrementAndGet();
		}

		public int accquire(int permits) {
			if (parent != null) {
				parent.accquire(permits);
			}
			return counter.addAndGet(permits);
		}

		public int release() {
			if (parent != null) {
				parent.release();
			}
			return counter.decrementAndGet();
		}

		public int release(int permits) {
			if (parent != null) {
				parent.release(permits);
			}
			return counter.addAndGet(-permits);
		}

		public int availablePermits() {
			int permits = Integer.MAX_VALUE;
			if (parent != null) {
				permits = parent.availablePermits();
			}
			return Integer.min(permits, maxPermits - counter.get());
		}
	}

}
