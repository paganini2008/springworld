package com.github.paganini2008.xtransport;

import java.util.List;

/**
 * 
 * Partitioner
 * 
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
public interface Partitioner {

	<T> T selectChannel(Object data, List<T> channels);

}
