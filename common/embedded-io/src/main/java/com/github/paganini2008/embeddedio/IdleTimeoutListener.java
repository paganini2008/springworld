package com.github.paganini2008.embeddedio;

import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;

/**
 * 
 * IdleTimeoutListener
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
@FunctionalInterface
public interface IdleTimeoutListener {

	static final Log logger = LogFactory.getLog(IdleTimeoutListener.class);

	void handleIdleTimeout(Channel channel, long timeout);

	static final IdleTimeoutListener LOG = new IdleTimeoutListener() {

		@Override
		public void handleIdleTimeout(Channel channel, long timeout) {
			logger.warn(channel.toString() + " is timeout.");
		}
	};

	static final IdleTimeoutListener CLOSE = new IdleTimeoutListener() {

		@Override
		public void handleIdleTimeout(Channel channel, long timeout) {
			logger.warn(channel.toString() + " is timeout.");
			channel.close();
		}
	};

}
