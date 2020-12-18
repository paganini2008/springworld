package com.github.paganini2008.springdessert.xtransport;

import static com.github.paganini2008.xtransport.Constants.SPRING_TRANSPORT_CLUSTER_NAMESPACE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springdessert.cluster.Constants;
import com.github.paganini2008.springdessert.cluster.ApplicationInfo;
import com.github.paganini2008.springdessert.reditools.BeanNames;
import com.github.paganini2008.xtransport.NioClient;
import com.github.paganini2008.xtransport.Partitioner;
import com.github.paganini2008.xtransport.Tuple;

/**
 * 
 * ApplicationTransportController
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@RequestMapping(ApplicationTransportController.PATH)
@RestController
public class ApplicationTransportController {

	public static final String PATH = "/application/cluster/transport";

	public static final String PATH_API = PATH + "/send";

	@Qualifier(BeanNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private NioClient nioClient;

	@Autowired
	private Partitioner partitioner;

	@Qualifier("producer")
	@Autowired
	private Counter producer;

	@Qualifier("consumer")
	@Autowired
	private Counter consumer;

	@Value("${spring.application.transport.httpserver.hostUrl:}")
	private String hostUrl;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@GetMapping("/send")
	public ResponseEntity<String> send(@RequestParam("message") String message) {
		Tuple data = Tuple.byString(message);
		nioClient.send(data, partitioner);
		return ResponseEntity.ok(message);
	}

	@GetMapping("/tps")
	public ResponseEntity<Map<String, Object>> tps() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("producer", new Object[] { producer.tps(false), producer.tps(true) });
		data.put("consumer", new Object[] { consumer.tps(false), consumer.tps(true) });
		return ResponseEntity.ok(data);
	}

	@GetMapping("/http/services")
	public ResponseEntity<String[]> httpServices() {
		String key = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		List<Object> dataList = redisTemplate.opsForList().range(key, 0, -1);
		List<Object> locations = new ArrayList<Object>();
		ApplicationInfo applicationInfo;
		String location;
		for (Object data : dataList) {
			applicationInfo = (ApplicationInfo) data;
			location = StringUtils.isNotBlank(hostUrl) ? hostUrl + PATH_API : applicationInfo.getApplicationContextPath() + PATH_API;
			locations.add(location);
		}
		return ResponseEntity.ok(locations.toArray(new String[0]));
	}

	@GetMapping("/tcp/services")
	public ResponseEntity<String[]> tcpServices() {
		String key = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		List<Object> dataList = redisTemplate.opsForList().range(key, 0, -1);
		List<Object> appKeys = new ArrayList<Object>();
		for (Object data : dataList) {
			appKeys.add(((ApplicationInfo) data).getId());
		}
		key = String.format(SPRING_TRANSPORT_CLUSTER_NAMESPACE, clusterName);
		List<Object> values = redisTemplate.opsForHash().multiGet(key, appKeys);
		String[] addresses = new String[values != null ? values.size() : 0];
		int i = 0;
		for (Object value : values) {
			addresses[i++] = (String) value;
		}
		return ResponseEntity.ok(addresses);
	}

}
