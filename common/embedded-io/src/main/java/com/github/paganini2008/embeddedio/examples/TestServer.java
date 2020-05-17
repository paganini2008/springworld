package com.github.paganini2008.embeddedio.examples;

import com.github.paganini2008.embeddedio.LoggingChannelHandler;
import com.github.paganini2008.embeddedio.NioAcceptor;
import com.github.paganini2008.embeddedio.ObjectSerialization;
import com.github.paganini2008.embeddedio.StringSerialization;

public class TestServer {

	public static void main(String[] args) throws Exception {
		NioAcceptor server = new NioAcceptor();
		server.getTransformer().setSerialization(new StringSerialization(), new ObjectSerialization());
		server.setReaderBufferSize(10 * 1024);
		LoggingChannelHandler handler = new LoggingChannelHandler("server");
		server.addHandler(handler);
		server.start();
		System.in.read();
		server.stop();
		System.out.println("TestServer.main()");
	}

}
