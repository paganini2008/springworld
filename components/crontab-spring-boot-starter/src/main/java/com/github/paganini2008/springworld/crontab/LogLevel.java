package com.github.paganini2008.springworld.crontab;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * 
 * LogLevel
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public enum LogLevel {

	TRACE {
		@Override
		public boolean canLog(Logger log) {
			return log.isTraceEnabled();
		}

		@Override
		public boolean canLog(Logger log, Marker marker) {
			return log.isTraceEnabled(marker);
		}

	},
	DEBUG {
		@Override
		public boolean canLog(Logger log) {
			return log.isDebugEnabled();
		}

		@Override
		public boolean canLog(Logger log, Marker marker) {
			return log.isDebugEnabled(marker);
		}
	},
	INFO {
		@Override
		public boolean canLog(Logger log) {
			return log.isInfoEnabled();
		}

		@Override
		public boolean canLog(Logger log, Marker marker) {
			return log.isInfoEnabled(marker);
		}
	},
	WARN {
		@Override
		public boolean canLog(Logger log) {
			return log.isWarnEnabled();
		}

		@Override
		public boolean canLog(Logger log, Marker marker) {
			return log.isWarnEnabled(marker);
		}
	},
	ERROR {
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

}
