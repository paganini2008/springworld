package com.github.paganini2008.springworld.joblink.model;

import com.github.paganini2008.devtools.comparator.ComparatorHelper;
import com.github.paganini2008.springworld.joblink.JobKey;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * JobPeer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Accessors(chain = true)
@Getter
@Setter
public class JobPeer implements Comparable<JobPeer> {

	private JobKey jobKey;
	private Object attachment;
	private float boost = 1F;

	public JobPeer() {
	}

	public JobPeer(JobKey jobKey, Object attachment, float boost) {
		this.jobKey = jobKey;
		this.attachment = attachment;
		this.boost = boost;
	}

	@Override
	public int compareTo(JobPeer other) {
		return ComparatorHelper.valueOf(boost - other.getBoost());
	}

}
