package com.github.paganini2008.embeddedio.examples;

import java.net.InetSocketAddress;
import java.util.UUID;

import com.github.paganini2008.embeddedio.Channel;
import com.github.paganini2008.embeddedio.ChannelHandler;
import com.github.paganini2008.embeddedio.LoggingChannelHandler;
import com.github.paganini2008.embeddedio.NioConnector;
import com.github.paganini2008.embeddedio.ObjectSerialization;
import com.github.paganini2008.embeddedio.ChannelPromise;
import com.github.paganini2008.embeddedio.StringSerialization;

public class TestClient {

	public static void main(String[] args) throws Exception {
		NioConnector client = new NioConnector();
		client.getTransformer().setSerialization(new ObjectSerialization(), new StringSerialization());
		client.setWriterBufferSize(20 * 1024);
		client.setWriterBatchSize(10);
		client.setAutoFlushInterval(3);
		ChannelHandler handler = new LoggingChannelHandler("client");
		client.addHandler(handler);
		Channel channel;
		try {
			channel = client.connect(new InetSocketAddress(8090), new ChannelPromise<Channel>() {

				@Override
				public void onSuccess(Channel channel) {
					System.out.println(channel + " is ok");
				}

				@Override
				public void onFailure(Throwable e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		System.in.read();
		for (int i = 0; i < 100000; i++) {
			channel.write(new Item("fengy_" + i, toFullString()));
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
