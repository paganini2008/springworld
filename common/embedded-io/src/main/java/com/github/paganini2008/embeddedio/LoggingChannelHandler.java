package com.github.paganini2008.embeddedio;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;

/**
 * 
 * LoggingChannelHandler
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public class LoggingChannelHandler implements ChannelHandler {

	private static final Log log = LogFactory.getLog(LoggingChannelHandler.class);

	private final String side;

	public LoggingChannelHandler(String side) {
		this.side = side;
	}

	@Override
	public void fireChannelActive(Channel channel) throws IOException {
		log.info("Channel is active. Channel info: " + channel);
	}

	@Override
	public void fireChannelInactive(Channel channel) throws IOException {
		log.info("Channel is inactive. Channel info: " + channel);
	}

	private final AtomicInteger counter = new AtomicInteger();

	@Override
	public void fireChannelReadable(Channel channel, MessagePacket packet) throws IOException {
		log.info("Channel read length: " + packet.getLength());
		packet.getMessages().forEach(data -> {
			log.info("[" + counter.incrementAndGet() + "]: " + data);
			if ("server".equals(side)) {
				//channel.write("ok");
			}
		});
	}

	@Override
	public void fireChannelWriteable(Channel channel, MessagePacket packet) throws IOException {
		//log.info("Channel write length: " + packet.getLength() + ", size: " + packet.getMessages().size());
	}

	@Override
	public void fireChannelFatal(Channel channel, Throwable e) {
		log.info("Channel has fatal error. Channel info: " + channel);
		log.error(e.getMessage(), e);
	}

}
