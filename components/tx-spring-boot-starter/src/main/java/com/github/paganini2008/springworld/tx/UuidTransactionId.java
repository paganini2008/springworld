package com.github.paganini2008.springworld.tx;

import java.util.UUID;

/**
 * 
 * UuidTransactionId
 *
 * @author Fred Feng
 * @version 1.0
 */
public class UuidTransactionId implements IdGenerator {

	@Override
	public String generateTransactionId() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	@Override
	public String generateXaTransactionId() {
		return "XA-" + UUID.randomUUID().toString().replace("-", "");
	}

}
