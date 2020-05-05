package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.date.DateUtils;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;

/**
 * 
 * IdleChannelHandler
 *
 * @author Fred Feng
 * @since 1.0
 */
public class IdleChannelHandler implements ChannelHandler {

	private final long readerTimeout;
	private final long writerTimeout;
	private final long checkInterval;
	private final IdleTimeoutListener idleTimeoutListener;

	public IdleChannelHandler(long readerTimeout, long writerTimeout, long checkInterval, TimeUnit timeUnit,
			IdleTimeoutListener idleTimeoutListener) {
		Assert.isNull(idleTimeoutListener, "Nullable IdleTimeoutListener");
		this.readerTimeout = DateUtils.convertToMillis(readerTimeout, timeUnit);
		this.writerTimeout = DateUtils.convertToMillis(writerTimeout, timeUnit);
		this.checkInterval = DateUtils.convertToMillis(checkInterval, timeUnit);
		this.idleTimeoutListener = idleTimeoutListener;
	}

	private volatile long lastRead;
	private volatile long lastWriten;
	private Timer timer;

	@Override
	public void fireChannelActive(final Channel channel) throws IOException {
		lastRead = System.currentTimeMillis();
		lastWriten = System.currentTimeMillis();
		
		timer = ThreadUtils.scheduleWithFixedDelay(() -> {
			long now = System.currentTimeMillis();
			if (readerTimeout > 0) {
				if (now - lastRead > readerTimeout) {
					idleTimeoutListener.handleIdleTimeout(channel, readerTimeout);
				}
			}
			if (writerTimeout > 0) {
				if (now - lastWriten > writerTimeout) {
					idleTimeoutListener.handleIdleTimeout(channel, writerTimeout);
				}
			}
			return channel.isActive();
		}, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
	}

	@Override
	public void fireChannelInactive(Channel channel) throws IOException {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@Override
	public void fireChannelReadable(Channel channel, MessagePacket packet) throws IOException {
		lastRead = System.currentTimeMillis();
	}

	@Override
	public void fireChannelWriteable(Channel channel, MessagePacket packet) throws IOException {
		lastWriten = System.currentTimeMillis();
	}

	public static ChannelHandler readerIdle(int readerTimeout, int checkInterval, TimeUnit timeUnit,
			IdleTimeoutListener idleTimeoutListener) {
		return new IdleChannelHandler(readerTimeout, 0, checkInterval, timeUnit, idleTimeoutListener);
	}

	public static ChannelHandler writerIdle(int writerTimeout, int checkInterval, TimeUnit timeUnit,
			IdleTimeoutListener idleTimeoutListener) {
		return new IdleChannelHandler(0, writerTimeout, checkInterval, timeUnit, idleTimeoutListener);
	}

}
