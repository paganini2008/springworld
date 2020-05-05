package com.github.paganini2008.embeddedio.test;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.paganini2008.embeddedio.AioConnector;
import com.github.paganini2008.embeddedio.ChannelHandler;
import com.github.paganini2008.embeddedio.IdleChannelHandler;
import com.github.paganini2008.embeddedio.IdleTimeoutListener;
import com.github.paganini2008.embeddedio.LoggingChannelHandler;
import com.github.paganini2008.embeddedio.ObjectSerialization;
import com.github.paganini2008.embeddedio.StringSerialization;
import com.github.paganini2008.embeddedio.examples.Item;

public class TestClient {

	public static void main(String[] args) throws Exception {
		AioConnector client = new AioConnector();
		client.getTransformer().setSerialization(new ObjectSerialization(), new StringSerialization());
		//client.setWriterBufferSize(20 * 1024);
		client.addHandler(IdleChannelHandler.writerIdle(30, 1, TimeUnit.SECONDS, IdleTimeoutListener.LOG));
		client.setWriterBatchSize(10);
		client.setAutoFlushInterval(3);
		ChannelHandler handler = new LoggingChannelHandler("client");
		client.addHandler(handler);
		try {
		client.connect(new InetSocketAddress("127.0.0.1",8090));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		System.in.read();
		for (int i = 0; i < 10000; i++) {
			client.write(new Item("fengy_" + i, toFullString()));
		}
		Thread.sleep(60 * 60 * 1000L);
		client.close();
		System.out.println("TestClient.main()");
	}

	private static String toFullString() {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			str.append(UUID.randomUUID().toString());
		}
		return str.toString();
	}

}
