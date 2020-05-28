package com.github.paganini2008.springworld.cluster.consistency;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * Court
 *
 * @author Fred Feng
 *
 * @since 1.0
 */
public class Court {

	private final Map<String, Proposal> juege = new ConcurrentHashMap<String, Proposal>();

	public boolean saveProposal(Proposal proposal) {
		if (juege.containsKey(proposal.getName())) {
			return false;
		}
		return juege.putIfAbsent(proposal.getName(), proposal) == null;
	}

	public Proposal getProposal(String name) {
		return juege.get(name);
	}

	public Proposal completeProposal(String name) {
		return juege.remove(name);
	}

	public void canLearn(ConsistencyResponse response) {
		ConsistencyRequest request = response.getRequest();
		if (juege.containsKey(request.getName())) {
			juege.get(request.getName()).getCommitments().add(response);
		}
	}

	public void canCommit(ConsistencyResponse response) {
		ConsistencyRequest request = response.getRequest();
		if (juege.containsKey(request.getName())) {
			juege.get(request.getName()).getPreparations().add(response);
		}
	}

	public static class Proposal {

		private final String name;
		private final Object value;
		private final List<ConsistencyResponse> preparations = new CopyOnWriteArrayList<ConsistencyResponse>();
		private final List<ConsistencyResponse> commitments = new CopyOnWriteArrayList<ConsistencyResponse>();

		Proposal(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		public List<ConsistencyResponse> getPreparations() {
			return preparations;
		}

		public List<ConsistencyResponse> getCommitments() {
			return commitments;
		}

		public String getName() {
			return name;
		}

		public Object getValue() {
			return value;
		}

	}

}
