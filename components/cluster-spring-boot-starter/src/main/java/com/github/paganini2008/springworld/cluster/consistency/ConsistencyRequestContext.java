package com.github.paganini2008.springworld.cluster.consistency;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.multithreads.Clock;
import com.github.paganini2008.devtools.multithreads.Clock.ClockTask;
import com.github.paganini2008.springworld.cluster.InstanceId;
import com.github.paganini2008.springworld.cluster.consistency.Court.Proposal;
import com.github.paganini2008.springworld.cluster.multicast.ClusterMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestContext
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public final class ConsistencyRequestContext {

	public static final int CONSISTENCY_REQUEST_MAX_TIMEOUT = 60;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private Clock clock;

	@Autowired
	private ClusterMulticastGroup clusterMulticastGroup;

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ConsistencyRequestSerial requestSerial;

	@Value("${spring.application.cluster.consistency.responseWaitingTime:1}")
	private long responseWaitingTime;

	@Autowired
	private Court court;

	public boolean propose(String name, Object value, int timeout) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Proposal name is a must.");
		}
		if (timeout > CONSISTENCY_REQUEST_MAX_TIMEOUT) {
			throw new IllegalArgumentException("Maximum timout is " + CONSISTENCY_REQUEST_MAX_TIMEOUT);
		}
		Proposal proposal = new Proposal(name, value);
		if (!court.saveProposal(proposal)) {
			log.warn("The proposal named '{}' is being processing currently. Please submit again after completion.", name);
			return false;
		}
		final long round = requestRound.currentRound(name);
		final long serial = requestSerial.nextSerial(name);
		ConsistencyRequest request = ConsistencyRequest.of(instanceId.getApplicationInfo()).setName(name).setRound(round).setSerial(serial)
				.setTimeout(timeout);
		clusterMulticastGroup.multicast(ConsistencyRequest.PREPARATION_OPERATION_REQUEST, request);
		clock.schedule(new ConsistencyRequestPreparationFuture(request), responseWaitingTime, TimeUnit.SECONDS);
		return true;
	}

	public void sync(String anotherInstanceId, String name, Object value) {
		final long round = requestRound.currentRound(name);
		final long serial = requestSerial.nextSerial(name);
		ConsistencyRequest request = ConsistencyRequest.of(instanceId.getApplicationInfo()).setName(name).setValue(value).setRound(round)
				.setSerial(serial).setTimeout(0);
		clusterMulticastGroup.send(anotherInstanceId, ConsistencyRequest.LEARNING_OPERATION_REQUEST, request);
	}

	private class ConsistencyRequestCommitmentFuture extends ClockTask {

		private final ConsistencyRequest request;

		ConsistencyRequestCommitmentFuture(ConsistencyRequest request) {
			this.request = request;
		}

		@Override
		protected void runTask() {
			final String name = request.getName();
			Proposal proposal = court.getProposal(name);
			List<ConsistencyResponse> original = proposal != null ? proposal.getCommitments() : null;
			List<ConsistencyResponse> expected = proposal != null ? proposal.getPreparations() : null;
			int originalLength = original != null ? original.size() : 0;
			int expectedLength = expected != null ? expected.size() : 0;
			if (originalLength > 0 && originalLength == expectedLength) {
				if (request.getRound() == requestRound.currentRound(name)) {
					proposal = court.getProposal(name);
					if (proposal != null) {
						long newRound = requestRound.nextRound(name);
						request.setRound(newRound);
						request.setValue(proposal.getValue());
						clusterMulticastGroup.multicast(ConsistencyRequest.LEARNING_OPERATION_REQUEST, request);
					}
				}
			} else {
				if (original != null) {
					original.clear();
				}
				if (expected != null) {
					expected.clear();
				}
				if (request.hasExpired()) {
					clusterMulticastGroup.multicast(ConsistencyRequest.TIMEOUT_OPERATION_REQUEST, request);
				} else {
					if (request.getRound() == requestRound.currentRound(request.getName())) {
						propose(request.getName(), request.getValue(), request.getTimeout());
					}
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
			final String name = request.getName();
			List<ConsistencyResponse> responses = court.getProposal(name) != null ? court.getProposal(name).getPreparations() : null;
			int n = clusterMulticastGroup.countOfChannel();
			if (responses != null && responses.size() > n / 2) {
				if (request.getRound() == requestRound.currentRound(name)) {
					for (ConsistencyResponse response : responses) {
						ConsistencyRequest request = response.getRequest();
						clusterMulticastGroup.send(response.getApplicationInfo().getId(), ConsistencyRequest.COMMITMENT_OPERATION_REQUEST,
								request);
					}
					clock.schedule(new ConsistencyRequestCommitmentFuture(request), responseWaitingTime, TimeUnit.SECONDS);
				}
			} else {
				if (responses != null) {
					responses.clear();
				}
				if (request.hasExpired()) {
					clusterMulticastGroup.multicast(ConsistencyRequest.TIMEOUT_OPERATION_REQUEST, request);
				} else {
					if (request.getRound() == requestRound.currentRound(request.getName())) {
						propose(request.getName(), request.getValue(), request.getTimeout());
					}
				}
			}
		}
	}

}
