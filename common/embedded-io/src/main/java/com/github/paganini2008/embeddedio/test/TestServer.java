package com.github.paganini2008.embeddedio.test;

import java.util.concurrent.TimeUnit;

import com.github.paganini2008.embeddedio.AioAcceptor;
import com.github.paganini2008.embeddedio.IdleChannelHandler;
import com.github.paganini2008.embeddedio.IdleTimeoutListener;
import com.github.paganini2008.embeddedio.LoggingChannelHandler;
import com.github.paganini2008.embeddedio.ObjectSerialization;
import com.github.paganini2008.embeddedio.StringSerialization;

public class TestServer {

	public static void main(String[] args) throws Exception {
		AioAcceptor server = new AioAcceptor();
		server.getTransformer().setSerialization(new StringSerialization(), new ObjectSerialization());
		server.setReaderBufferSize(10 * 1024);
		LoggingChannelHandler handler = new LoggingChannelHandler("server");
		server.addHandler(IdleChannelHandler.readerIdle(60, 60, TimeUnit.SECONDS, IdleTimeoutListener.LOG));
		server.addHandler(handler);
		server.start();
		System.in.read();
		server.stop();
		System.out.println("TestServer.main()");
	}

}
