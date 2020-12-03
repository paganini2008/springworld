package com.github.paganini2008.springdessert.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * GitConfigProperties
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class GitConfigProperties extends GitRepoProperties {

	private static final long serialVersionUID = 3977102427571801020L;

	private final Map<String, String> defaultProperties;

	public GitConfigProperties() {
		this(new HashMap<String, String>());
	}

	public GitConfigProperties(Map<String, String> defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	protected Properties createObject() throws Exception {
		Properties p = super.createObject();
		for (Map.Entry<String, String> entry : defaultProperties.entrySet()) {
			p.setProperty(entry.getKey(), entry.getValue());
		}
		return p;
	}

	public Map<String, String> getDefaultProperties() {
		return defaultProperties;
	}

}
