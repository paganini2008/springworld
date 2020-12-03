package com.github.paganini2008.xtransport;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.LruQueue;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.logging.Log;
import com.github.paganini2008.devtools.logging.LogFactory;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 
 * DefaultHttpClient
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class DefaultHttpClient implements HttpClient, Executable {

	private static final Log logger = LogFactory.getLog(HttpTransportClient.class);
	private static final int MAX_MESSAGE_QUEUE_BUFFER_SIZE = 1024;
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final List<String> channels = new CopyOnWriteArrayList<String>();
	private final Map<String, Queue<Object>> retryQueue = new ConcurrentHashMap<String, Queue<Object>>();
	private final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
	private OkHttpClient client;
	private Timer timer;

	@Override
	public void open() {
		timer = ThreadUtils.scheduleAtFixedRate(this, 3, TimeUnit.SECONDS);
		client = clientBuilder.build();
	}

	@Override
	public void setConnectionTimeout(int timeout) {
		clientBuilder.connectTimeout(timeout, TimeUnit.SECONDS);
	}

	@Override
	public void setReadTimeout(int timeout) {
		clientBuilder.readTimeout(20, TimeUnit.SECONDS);
	}

	@Override
	public void close() {
		if (timer != null) {
			timer.cancel();
		}
		client = null;
	}

	@Override
	public void send(Object data) {
		for (String channel : channels) {
			doSend(channel, data);
		}
	}

	@Override
	public void send(Object data, Partitioner partitioner) {
		final String channel = partitioner.selectChannel(data, channels);
		if (StringUtils.isBlank(channel)) {
			return;
		}
		doSend(channel, data);
	}

	private void doSend(final String channel, final Object data) {
		String jsonString;
		try {
			jsonString = objectMapper.writeValueAsString(data);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}
		Request request = new Request.Builder().url(channel).post(RequestBody.create(JSON, jsonString)).build();
		Call call = client.newCall(request);
		call.enqueue(new Callback() {

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				response.close();
			}

			@Override
			public void onFailure(Call call, IOException e) {
				logger.error(e.getMessage(), e);
				final Queue<Object> q = MapUtils.get(retryQueue, channel, () -> {
					return new LruQueue<Object>(MAX_MESSAGE_QUEUE_BUFFER_SIZE);
				});
				q.add(data);
			}
		});
	}

	@Override
	public void addChannel(String channel) {
		if (!channels.contains(channel)) {
			channels.add(channel);
		}
	}

	@Override
	public void clearChannels() {
		channels.clear();
	}

	@Override
	public boolean execute() {
		if (retryQueue.isEmpty()) {
			return true;
		}
		String channel;
		Queue<Object> q;
		for (Map.Entry<String, Queue<Object>> entry : retryQueue.entrySet()) {
			channel = entry.getKey();
			q = new ArrayDeque<Object>(entry.getValue());
			for (Object data : q) {
				entry.getValue().remove(data);
				doSend(channel, data);
			}
		}
		return true;
	}

}
