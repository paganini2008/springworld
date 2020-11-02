package com.github.paganini2008.springworld.jobsoup;

import com.github.paganini2008.springworld.jobsoup.server.ServerModeConfiguration;
import com.github.paganini2008.springworld.jobsoup.ui.UIModeConfiguration;

/**
 * 
 * DeployMode
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum DeployMode {

	EMBEDDED(EmbeddedModeConfiguration.class.getName()),

	SERVER(ServerModeConfiguration.class.getName()),

	UI(UIModeConfiguration.class.getName());

	private final String configurationClassName;

	private DeployMode(String configurationClassName) {
		this.configurationClassName = configurationClassName;
	}

	public String getConfigurationClassName() {
		return configurationClassName;
	}

}
