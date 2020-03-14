package com.github.paganini2008.springworld.transport.transport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springworld.transport.Counter;
import com.github.paganini2008.springworld.transport.buffer.BufferZone;
import com.github.paganini2008.transport.ChannelEvent;
import com.github.paganini2008.transport.ChannelEvent.EventType;
import com.github.paganini2008.transport.ChannelEventListener;
import com.github.paganini2008.transport.Tuple;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * NettyServerHandler
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

	@Autowired
	private BufferZone store;
	
	@Qualifier("local-counter")
	@Autowired
	private Counter counter;

	@Value("${spring.transport.bufferzone.collectionName:default}")
	private String collectionName;

	@Autowired(required = false)
	private ChannelEventListener<Channel> channelEventListener;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		fireChannelEvent(ctx.channel(), EventType.CONNECTED, null);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		fireChannelEvent(ctx.channel(), EventType.CLOSED, null);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		log.error(cause.getMessage(), cause);
		fireChannelEvent(ctx.channel(), EventType.FAULTY, cause);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
		counter.incrementAndGet();
		
		Tuple data = (Tuple) message;
		String collectionName = (String) data.getField(Tuple.KEYWORD_COLLECTION, this.collectionName);
		store.set(collectionName, data);
	}

	private void fireChannelEvent(Channel channel, EventType eventType, Throwable cause) {
		if (channelEventListener != null) {
			channelEventListener.fireChannelEvent(new ChannelEvent<Channel>(channel, eventType, cause));
		}
	}

}
