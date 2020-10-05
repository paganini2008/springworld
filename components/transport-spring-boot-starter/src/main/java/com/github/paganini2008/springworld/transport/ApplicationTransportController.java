package com.github.paganini2008.springworld.transport;

import static com.github.paganini2008.transport.Constants.APPLICATION_KEY;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.springworld.cluster.ApplicationClusterAware;
import com.github.paganini2008.springworld.cluster.ApplicationInfo;
import com.github.paganini2008.springworld.reditools.BeanNames;
import com.github.paganini2008.transport.NioClient;
import com.github.paganini2008.transport.Partitioner;
import com.github.paganini2008.transport.Tuple;

/**
 * 
 * ApplicationTransportController
 * 
 * @author Fred Feng
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
	private StringRedisTemplate stringRedisTemplate;

	@Value("${spring.application.cluster.name:default}")
	private String clusterName;

	@Autowired
	private NioClient nioClient;

	@Autowired
	private Partitioner partitioner;

	@Value("${spring.application.transport.httpserver.hostUrl:}")
	private String hostUrl;

	@PostMapping("/send")
	public ResponseEntity<String> send(@RequestBody String message) {
		Tuple data = Tuple.byString(message);
		nioClient.send(data, partitioner);
		return ResponseEntity.ok("ok");
	}

	@GetMapping("/http/services")
	public ResponseEntity<String[]> httpServices() {
		String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName;
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
		String key = ApplicationClusterAware.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		List<Object> dataList = redisTemplate.opsForList().range(key, 0, -1);
		List<Object> appKeys = new ArrayList<Object>();
		for (Object data : dataList) {
			appKeys.add(((ApplicationInfo) data).getId());
		}
		key = APPLICATION_KEY + clusterName;
		List<Object> values = stringRedisTemplate.opsForHash().multiGet(key, appKeys);
		String[] addresses = new String[values != null ? values.size() : 0];
		int i = 0;
		for (Object value : values) {
			addresses[i++] = (String) value;
		}
		return ResponseEntity.ok(addresses);
	}

}
