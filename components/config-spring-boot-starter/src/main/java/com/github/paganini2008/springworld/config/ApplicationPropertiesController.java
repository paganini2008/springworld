package com.github.paganini2008.springworld.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.paganini2008.devtools.collection.MapUtils;

/**
 * 
 * ApplicationPropertiesController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/config")
public class ApplicationPropertiesController {

	@Autowired
	private ApplicationProperties applicationProperties;

	@GetMapping("")
	public ResponseEntity<Map<Object, Object>> config() {
		return ResponseEntity.ok(MapUtils.sort(applicationProperties, (left, right) -> {
			String leftKey = (String) left.getKey();
			String rightKey = (String) right.getKey();
			return leftKey.compareTo(rightKey);
		}));
	}

}
