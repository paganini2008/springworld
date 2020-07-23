package com.github.paganini2008.springworld.transport.transport;

import java.io.IOException;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.transport.Counter;
import com.github.paganini2008.springworld.transport.buffer.BufferZone;
import com.github.paganini2008.transport.ChannelEvent;
import com.github.paganini2008.transport.ChannelEvent.EventType;
import com.github.paganini2008.transport.ChannelEventListener;
import com.github.paganini2008.transport.Tuple;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * GrizzlyServerHandler
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class GrizzlyServerHandler extends BaseFilter {

	@Value("${spring.application.transport.nioserver.keepalive.response:true}")
	private boolean keepaliveResposne;

	@Autowired
	private BufferZone bufferZone;
	
	@Autowired
	private Counter counter;

	@Value("${spring.application.transport.bufferzone.collectionName:default}")
	private String collectionName;

	@Autowired(required = false)
	private ChannelEventListener<Connection<?>> channelEventListener;

	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		Tuple message = ctx.getMessage();
		if (isPing(message)) {
			if (channelEventListener != null) {
				channelEventListener.fireChannelEvent(new ChannelEvent<Connection<?>>(ctx.getConnection(), EventType.PING, null));
			}
			if (keepaliveResposne) {
				ctx.write(Tuple.PONG);
			}
			return ctx.getStopAction();
		} else {
			counter.increment();
			try {
				bufferZone.set(collectionName, message);
			} catch (Exception e) {
				if (e instanceof IOException) {
					throw (IOException) e;
				}
				throw new IOException(e);
			}
			return ctx.getInvokeAction();
		}
	}

	protected boolean isPing(Object data) {
		return (data instanceof Tuple) && ((Tuple) data).isPing();
	}

	@Override
	public NextAction handleAccept(FilterChainContext ctx) throws IOException {
		fireChannelEvent(ctx.getConnection(), EventType.CONNECTED, null);
		return ctx.getInvokeAction();
	}

	@Override
	public NextAction handleClose(FilterChainContext ctx) throws IOException {
		fireChannelEvent(ctx.getConnection(), EventType.CLOSED, null);
		return ctx.getInvokeAction();
	}

	@Override
	public void exceptionOccurred(FilterChainContext ctx, Throwable cause) {
		log.error(cause.getMessage(), cause);
		fireChannelEvent(ctx.getConnection(), EventType.FAULTY, cause);
	}

	private void fireChannelEvent(Connection<?> channel, EventType eventType, Throwable cause) {
		if (channelEventListener != null) {
			channelEventListener.fireChannelEvent(new ChannelEvent<Connection<?>>(channel, eventType, cause));
		}
	}

}
