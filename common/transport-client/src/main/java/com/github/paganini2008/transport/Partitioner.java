package com.github.paganini2008.transport;

import java.util.List;

/**
 * 
 * Partitioner
 * 
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
public interface Partitioner {

	<T> T selectChannel(Object data, List<T> channels);

}
