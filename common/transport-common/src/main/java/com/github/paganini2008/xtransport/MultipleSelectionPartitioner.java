package com.github.paganini2008.xtransport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.StringUtils;

/**
 * 
 * MultipleSelectionPartitioner
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class MultipleSelectionPartitioner implements Partitioner {

	private final Map<String, Partitioner> selector = new ConcurrentHashMap<String, Partitioner>();

	public MultipleSelectionPartitioner() {
		selector.put("roundrobin", new RoundRobinPartitioner());
		selector.put("random", new RandomPartitioner());
	}

	private Partitioner defaultPartitioner = new RoundRobinPartitioner();

	public void setDefaultPartitioner(Partitioner defaultPartitioner) {
		Assert.isNull(defaultPartitioner, "Default partitioner must not be null.");
		this.defaultPartitioner = defaultPartitioner;
	}

	public void addPartitioner(String type, Partitioner partitioner) {
		Assert.isNull(partitioner, "Partitioner must not be null.");
		selector.put(type, partitioner);
	}

	@Override
	public <T> T selectChannel(Object data, List<T> channels) {
		if (data instanceof Tuple) {
			Tuple tuple = (Tuple) data;
			String partitionerType = tuple.getPartitionerType();
			Partitioner partitioner;
			if (StringUtils.isNotBlank(partitionerType) && null != (partitioner = selector.get(partitionerType))) {
				return partitioner.selectChannel(data, channels);
			}
		}
		return defaultPartitioner.selectChannel(data, channels);
	}

}
