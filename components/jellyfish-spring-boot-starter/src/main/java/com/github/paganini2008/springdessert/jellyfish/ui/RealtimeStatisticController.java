package com.github.paganini2008.springdessert.jellyfish.ui;

import static com.github.paganini2008.springdessert.jellyfish.Utils.encodeString;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.jellyfish.stat.Catalog;
import com.github.paganini2008.springdessert.jellyfish.stat.TransientStatisticSynchronizer;

/**
 * 
 * RealtimeStatisticController
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@RequestMapping("/application/cluster/metric")
@RestController
public class RealtimeStatisticController {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@PostMapping("/query")
	public Response query(@RequestBody Catalog catalog) {
		String key = String.format(TransientStatisticSynchronizer.KEY_TOTAL_SUMMARY, catalog.getClusterName(), catalog.getApplicationName(),
				encodeString(catalog.getHost()), encodeString(catalog.getPath()));
		Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
		return Response.success(data);
	}

	@PostMapping("/{metric}/query")
	public Response realtimeQuery(@PathVariable("metric") String metric, @RequestBody Catalog catalog) {
		String key = String.format(TransientStatisticSynchronizer.KEY_REALTIME_SUMMARY, metric, catalog.getClusterName(),
				catalog.getApplicationName(), encodeString(catalog.getHost()), encodeString(catalog.getPath()));
		Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
		if (MapUtils.isNotEmpty(data)) {
			data = new TreeMap<Object, Object>(data);
		}
		return Response.success(data);
	}

}
