package com.github.paganini2008.springworld.jobswarm;

import org.slf4j.Logger;
import org.slf4j.Marker;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.paganini2008.devtools.enums.EnumConstant;

/**
 * 
 * LogLevel
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum LogLevel implements EnumConstant {

	TRACE(0, "trace") {
		@Override
		public boolean canLog(Logger log) {
			return log.isTraceEnabled();
		}

		@Override
		public boolean canLog(Logger log, Marker marker) {
			return log.isTraceEnabled(marker);
		}

	},
	DEBUG(1, "debug") {
		@Override
		public boolean canLog(Logger log) {
			return log.isDebugEnabled();
		}

		@Override
		public boolean canLog(Logger log, Marker marker) {
			return log.isDebugEnabled(marker);
		}
	},
	INFO(2, "info") {
		@Override
		public boolean canLog(Logger log) {
			return log.isInfoEnabled();
		}

		@Override
		public boolean canLog(Logger log, Marker marker) {
			return log.isInfoEnabled(marker);
		}
	},
	WARN(3, "warn") {
		@Override
		public boolean canLog(Logger log) {
			return log.isWarnEnabled();
		}

		@Override
		public boolean canLog(Logger log, Marker marker) {
			return log.isWarnEnabled(marker);
		}
	},
	ERROR(4, "error") {
		@Override
		public boolean canLog(Logger log) {
			return log.isErrorEnabled();
		}

		@Override
		public boolean canLog(Logger log, Marker marker) {
			return log.isErrorEnabled(marker);
		}

	};

	public abstract boolean canLog(Logger log);

	public abstract boolean canLog(Logger log, Marker marker);

	private final int value;
	private final String repr;

	private LogLevel(int value, String repr) {
		this.value = value;
		this.repr = repr;
	}

	@JsonValue
	public int getValue() {
		return value;
	}

	@Override
	public String getRepr() {
		return repr;
	}

	@JsonCreator
	public static LogLevel valueOf(int value) {
		for (LogLevel logLevel : LogLevel.values()) {
			if (logLevel.getValue() == value) {
				return logLevel;
			}
		}
		throw new IllegalArgumentException("Unknown logLevel: " + value);
	}

}
