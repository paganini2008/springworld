package com.github.paganini2008.springworld.cluster;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.io.IOUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ClusterId
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public final class ClusterId {

	@Autowired
	private ClusterIdGenerator idGenerator;

	@Value("${spring.application.cluster.id:}")
	private String id;

	@Getter
	@Value("${spring.application.cluster.weight:1}")
	private int weight;

	private final AtomicBoolean master = new AtomicBoolean(false);

	public String get() {
		if (StringUtils.isBlank(id)) {
			synchronized (this) {
				if (StringUtils.isBlank(id)) {
					id = idGenerator.generateClusterId();
					log.info(IOUtils.NEWLINE + "\tGenerate the clusterId: " + id);
				}
			}
		}
		return id;
	}

	public boolean isMaster() {
		return master.get();
	}

	public void setMaster(boolean master) {
		this.master.set(master);
	}

	public String toString() {
		return "ClusterId: " + get() + ", Master: " + isMaster();
	}

}
