package com.github.paganini2008.transport.grizzly;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.utils.IdleTimeoutFilter;

import com.github.paganini2008.transport.KeepAliveTimeoutException;
import com.github.paganini2008.transport.Tuple;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * IdleTimeoutHandlers
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@SuppressWarnings("all")
public abstract class IdleTimeoutHandlers {

	public static IdleTimeoutFilter.TimeoutHandler PING = new IdleTimeoutFilter.TimeoutHandler() {

		private final AtomicLong serial = new AtomicLong(0);

		public void onTimeout(Connection connection) {
			Tuple ping = Tuple.byString("PING");
			ping.setField("serial", serial.incrementAndGet());
			connection.write(ping);
		}

	};

	public static IdleTimeoutFilter.TimeoutHandler LOG = new IdleTimeoutFilter.TimeoutHandler() {

		public void onTimeout(Connection connection) {
			log.warn("A keep-alive response message was not received within {} second(s).", connection.getReadTimeout(TimeUnit.SECONDS));
		}

	};

	public static IdleTimeoutFilter.TimeoutHandler EXCEPTION = new IdleTimeoutFilter.TimeoutHandler() {

		public void onTimeout(Connection connection) {
			throw new KeepAliveTimeoutException(
					"A keep-alive response message was not received within " + connection.getWriteTimeout(TimeUnit.SECONDS) + " second(s).");
		}

	};

	public static IdleTimeoutFilter.TimeoutHandler NOOP = new IdleTimeoutFilter.TimeoutHandler() {

		public void onTimeout(Connection connection) {
		}

	};

}
