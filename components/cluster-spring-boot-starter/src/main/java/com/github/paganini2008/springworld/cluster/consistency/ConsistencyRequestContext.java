package com.github.paganini2008.springworld.cluster.consistency;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.collection.LruMap;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.devtools.multithreads.Clock.ClockTask;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

/**
 * 
 * ConsistencyRequestContext
 *
 * @author Fred Feng
 * @since 1.0
 */
public final class ConsistencyRequestContext {

	@Autowired
	private InstanceId clusterId;

	@Autowired
	private Clock clock;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ConsistencyRequestSerial requestSerial;

	@Value("${spring.application.cluster.consistency.responseWaitingTime:3}")
	private long responseWaitingTime;

	private final Map<String, Map<Long, List<ConsistencyResponse>>> preparations = new LruMap<String, Map<Long, List<ConsistencyResponse>>>();
	private final Map<String, Map<Long, List<ConsistencyResponse>>> commitments = new LruMap<String, Map<Long, List<ConsistencyResponse>>>();

	public void canLearn(ConsistencyResponse response) {
		ConsistencyRequest request = response.getRequest();
		Map<Long, List<ConsistencyResponse>> map = MapUtils.get(commitments, request.getName(), () -> {
			return new LruMap<Long, List<ConsistencyResponse>>(16);
		});
		MapUtils.get(map, request.getRound(), () -> {
			return new CopyOnWriteArrayList<ConsistencyResponse>();
		}).add(response);
	}

	public void canCommit(ConsistencyResponse response) {
		ConsistencyRequest request = response.getRequest();
		Map<Long, List<ConsistencyResponse>> map = MapUtils.get(preparations, request.getName(), () -> {
			return new LruMap<Long, List<ConsistencyResponse>>();
		});
		MapUtils.get(map, request.getRound(), () -> {
			return new CopyOnWriteArrayList<ConsistencyResponse>();
		}).add(response);
	}

	public void propose(String name, Object value) {
		final long round = requestRound.currentRound(name);
		final long serial = requestSerial.nextSerial(name);
		ConsistencyRequest request = ConsistencyRequest.of(clusterId.get()).setName(name).setValue(value).setRound(round).setSerial(serial);
		clusterMulticastGroup.multicast(ConsistencyRequest.PREPARATION_OPERATION_REQUEST, request);
		clock.schedule(new ConsistencyRequestPreparationFuture(request), responseWaitingTime, TimeUnit.SECONDS);
	}

	private class ConsistencyRequestCommitmentFuture extends ClockTask {

		private final ConsistencyRequest request;

		ConsistencyRequestCommitmentFuture(ConsistencyRequest request) {
			this.request = request;
		}

		@Override
		protected void runTask() {
			String name = request.getName();
			long round = request.getRound();

			List<ConsistencyResponse> original = commitments.containsKey(name) ? commitments.get(name).get(round) : null;
			List<ConsistencyResponse> expected = preparations.containsKey(name) ? preparations.get(name).get(round) : null;
			int originalLength = original != null ? original.size() : 0;
			int expectedLength = expected != null ? expected.size() : 0;
			if (originalLength > 0 && originalLength == expectedLength) {
				long newRound = requestRound.nextRound(request.getName());
				request.setRound(newRound);
				clusterMulticastGroup.multicast(ConsistencyRequest.LEARNING_OPERATION_REQUEST, request);
			} else {
				if (original != null) {
					original.clear();
				}
				if (expected != null) {
					expected.clear();
				}

				if (request.getRound() == requestRound.currentRound(request.getName())) {
					propose(request.getName(), request.getValue());
				}
			}
		}

	}

	private class ConsistencyRequestPreparationFuture extends ClockTask {

		private final ConsistencyRequest request;

		ConsistencyRequestPreparationFuture(ConsistencyRequest request) {
			this.request = request;
		}

		@Override
		protected void runTask() {
			String name = request.getName();
			long round = request.getRound();

			List<ConsistencyResponse> answers = preparations.containsKey(name) ? preparations.get(name).get(round) : null;
			int n = clusterMulticastGroup.countOfChannel();
			if (answers != null && answers.size() > n / 2) {
				for (ConsistencyResponse response : answers) {
					ConsistencyRequest request = response.getRequest();
					clusterMulticastGroup.send(response.getInstanceId(), ConsistencyRequest.COMMITMENT_OPERATION_REQUEST, request);
				}
				clock.schedule(new ConsistencyRequestCommitmentFuture(request), responseWaitingTime, TimeUnit.SECONDS);
			} else {
				if (answers != null) {
					answers.clear();
				}

				if (request.getRound() == requestRound.currentRound(request.getName())) {
					propose(request.getName(), request.getValue());
				}
			}
		}
	}

}
