package com.github.paganini2008.xtransport;

import java.util.Map;

/**
 * 
 * MdcHashPartitioner
 * 
 * @author Jimmy Hoff
 * 
 * 
 * @version 1.0
 */
public class MdcHashPartitioner extends HashPartitioner {

	@SuppressWarnings("rawtypes")
	@Override
	protected Object getFieldValue(Tuple tuple, String fieldName) {
		Object result = super.getFieldValue(tuple, fieldName);
		if (result == null) {
			Map mdc = (Map) tuple.getField("mdc");
			if (mdc != null) {
				result = mdc.get(fieldName);
			}
		}
		return result;
	}

}