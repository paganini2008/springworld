package com.github.paganini2008.springdessert.cooper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.net.NetUtils;
import com.github.paganini2008.xtransport.TransportClient;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * RealtimeStatisticalWriter
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class RealtimeStatisticalWriter extends StatisticalWriter {

	private static final String TOPIC_NAME = RealtimeStatisticalWriter.class.getName();

	private final ConcurrentMap<String, AtomicInteger> concurrencies = new ConcurrentHashMap<String, AtomicInteger>();

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${server.port}")
	private int port;

	@Autowired
	private PathMatchedMap timeouts;

	@Autowired
	private TransportClient transportClient;

	private String host = NetUtils.getLocalHost();

	@Override
	protected void onRequestBegin(HttpServletRequest request, String requestId) throws Exception {
		getConcurrency(request.getServletPath()).incrementAndGet();
	}

	@Override
	protected void onRequestEnd(HttpServletRequest request, String requestId, Exception e) throws Exception {
		if (StringUtils.isBlank(requestId)) {
			return;
		}
		Long begin = (Long) request.getAttribute(REQUEST_TIMESTAMP);
		if (begin == null) {
			return;
		}
		final String path = request.getServletPath();
		long elapsed = System.currentTimeMillis() - begin.longValue();
		int concurrency = getConcurrency(path).decrementAndGet();
		Map<String, Object> contextMap = new HashMap<String, Object>();
		contextMap.put(Tuple.KEYWORD_TOPIC, TOPIC_NAME);
		contextMap.put(Tuple.KEYWORD_PARTITIONER, "hash");
		contextMap.put("requestId", requestId);
		contextMap.put("clusterName", clusterName);
		contextMap.put("applicationName", applicationName);
		contextMap.put("host", host + ":" + port);
		contextMap.put("path", path);
		contextMap.put("requestTime", begin.longValue());
		contextMap.put("elapsed", elapsed);
		contextMap.put("timeout", isTimeout(path, (Long) request.getAttribute(REQUEST_TIMESTAMP)));
		contextMap.put("failed", e != null);
		contextMap.put("concurrency", concurrency);
		transportClient.send(Tuple.wrap(contextMap));

	}

	private boolean isTimeout(String path, long requestTime) {
		boolean timeout = false;
		if (timeouts.containsKey(path)) {
			long elapsed = System.currentTimeMillis() - requestTime;
			timeout = elapsed > timeouts.get(path);
		}
		return timeout;
	}

	private AtomicInteger getConcurrency(String path) {
		return MapUtils.get(concurrencies, path, () -> new AtomicInteger(0));
	}

}
