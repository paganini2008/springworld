package com.github.paganini2008.springworld.transport;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.transport.NioClient;
import com.github.paganini2008.transport.Partitioner;
import com.github.paganini2008.transport.Tuple;

import io.netty.util.internal.ThreadLocalRandom;

/**
 * 
 * BenchmarkController
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@RequestMapping("/transport")
@RestController
public class BenchmarkController {

	@Autowired
	private NioClient nioClient;

	@Autowired
	private Partitioner partitioner;

	@GetMapping("/echo")
	public Map<String, Object> echo(@RequestParam("q") String content) {
		Tuple data = Tuple.byString(UUID.randomUUID().toString());
		nioClient.send(data, partitioner);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("q", content);
		result.put("success", true);
		return result;
	}

	@GetMapping("/test")
	public Map<String, Object> test(@RequestParam(name = "n", defaultValue = "10000", required = false) int N) {
		for (int i = 0; i < N; i++) {
			StringBuilder str = new StringBuilder();
			for (int j = 0, l = ThreadLocalRandom.current().nextInt(1, 10); j < l; j++) {
				str.append(UUID.randomUUID().toString());
			}
			Tuple data = Tuple.byString(str.toString());
			nioClient.send(data, partitioner);
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", true);
		return result;
	}

}
