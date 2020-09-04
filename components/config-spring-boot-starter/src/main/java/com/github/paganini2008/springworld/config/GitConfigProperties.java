package com.github.paganini2008.springworld.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * GitConfigProperties
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class GitConfigProperties extends GitRepoProperties {

	private static final long serialVersionUID = 3977102427571801020L;

	private final Map<String, String> defaultConfiguration;

	public GitConfigProperties() {
		this(new HashMap<String, String>());
	}

	public GitConfigProperties(Map<String, String> defaultConfig) {
		this.defaultConfiguration = defaultConfig;
	}

	protected Properties createObject() throws Exception {
		Properties p = super.createObject();
		for (Map.Entry<String, String> entry : defaultConfiguration.entrySet()) {
			p.setProperty(entry.getKey(), entry.getValue());
		}
		return p;
	}

	public Map<String, String> getDefaultConfiguration() {
		return defaultConfiguration;
	}

	protected void sort(File[] files) {
		ApplicationPropertiesLoadingComparator.sort(files);
	}

}
