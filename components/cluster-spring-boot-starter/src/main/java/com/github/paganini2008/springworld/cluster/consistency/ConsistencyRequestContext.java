package com.github.paganini2008.springworld.cluster.consistency;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.devtools.multithreads.Clock.ClockTask;
import com.github.paganini2008.springworld.cluster.ClusterId;
import com.github.paganini2008.springworld.cluster.multicast.ContextMulticastGroup;

/**
 * 
 * ConsistencyRequestContext
 *
 * @author Fred Feng
 * @since 1.0
 */
public final class ConsistencyRequestContext {

	@Autowired
	private ClusterId clusterId;

	@Autowired
	private Clock clock;

	@Autowired
	private ContextMulticastGroup contextMulticastGroup;

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ConsistencyRequestSerial requestSerial;

	private final Map<String, List<ConsistencyResponse>> preparations = new ConcurrentHashMap<String, List<ConsistencyResponse>>();
	private final Map<String, List<ConsistencyResponse>> commitments = new ConcurrentHashMap<String, List<ConsistencyResponse>>();

	public void canLearn(ConsistencyResponse response) {
		final String id = response.getRequest().getId();
		commitments.getOrDefault(id, new CopyOnWriteArrayList<ConsistencyResponse>()).add(response);
	}

	public void canCommit(ConsistencyResponse response) {
		final String id = response.getRequest().getId();
		preparations.getOrDefault(id, new CopyOnWriteArrayList<ConsistencyResponse>()).add(response);
	}

	public void propose(String name, Object value) {
		final long round = requestRound.currentRound(name);
		final long serial = requestSerial.nextSerial(name);
		ConsistencyRequest request = ConsistencyRequest.of(clusterId.get()).setName(name).setValue(value).setRound(round).setSerial(serial);
		contextMulticastGroup.multicast(ConsistencyRequest.PREPARATION_REQUEST, request);
		clock.schedule(new ConsistencyRequestPreparationFuture(request), 2, TimeUnit.SECONDS);
	}

	private class ConsistencyRequestCommitmentFuture extends ClockTask {

		private final ConsistencyRequest request;

		ConsistencyRequestCommitmentFuture(ConsistencyRequest request) {
			this.request = request;
		}

		@Override
		protected void runTask() {
			List<ConsistencyResponse> original = commitments.get(request.getId());
			List<ConsistencyResponse> expected = preparations.get(request.getId());
			if (original.size() == expected.size()) {
				long newRound = requestRound.nextRound(request.getName());
				request.setRound(newRound);
				contextMulticastGroup.multicast(ConsistencyRequest.LEARNING_REQUEST, request);
			} else {
				original.clear();
				expected.clear();

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
			List<ConsistencyResponse> answers = preparations.get(request.getId());
			int n = contextMulticastGroup.countOfChannel();
			if (answers.size() > n / 2) {
				for (ConsistencyResponse response : answers) {
					ConsistencyRequest request = response.getRequest();
					contextMulticastGroup.send(response.getInstanceId(), ConsistencyRequest.COMMITMENT_REQUEST, request);
				}
				clock.schedule(new ConsistencyRequestCommitmentFuture(request.copy()), 2, TimeUnit.SECONDS);
			} else {
				answers.clear();

				if (request.getRound() == requestRound.currentRound(request.getName())) {
					propose(request.getName(), request.getValue());
				}
			}
		}
	}

}
