package com.github.paganini2008.springdessert.xtransport;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.ArrayUtils;
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
@RequestMapping("/application/cluster/transport")
@RestController
public class ApplicationTransportController {

	@Autowired
	private NioClient nioClient;

	@Autowired
	private Partitioner partitioner;

	@Autowired
	private ApplicationTransportContext context;

	@Qualifier("producer")
	@Autowired
	private Counter producer;

	@Qualifier("consumer")
	@Autowired
	private Counter consumer;

	@GetMapping("/send")
	public ResponseEntity<String> send(@RequestParam("message") String message) {
		Tuple data = Tuple.byString(message);
		nioClient.send(data, partitioner);
		return ResponseEntity.ok(message);
	}

	@GetMapping("/tps")
	public ResponseEntity<Map<String, Object>> tps() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("producer", producer.getTps() + "/" + producer.getTotalTps());
		data.put("consumer", consumer.getTps() + "/" + consumer.getTotalTps());
		return ResponseEntity.ok(data);
	}

	@GetMapping("/tcp/services")
	public ResponseEntity<String[]> tcpServices() {
		ServerInfo[] serverInfos = context.getServerInfos();
		String[] services = ArrayUtils.map(serverInfos, info -> {
			return info.toString();
		});
		return ResponseEntity.ok(services);
	}

}
