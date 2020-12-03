package com.github.paganini2008.springdessert.jobsoup;

import com.github.paganini2008.springdessert.jobsoup.server.ServerModeConfiguration;
import com.github.paganini2008.springdessert.jobsoup.ui.UIModeConfiguration;

/**
 * 
 * DeployMode
 * 
 * @author Jimmy Hoff
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
