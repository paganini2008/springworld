package com.github.paganini2008.springdessert.tx;

import java.util.UUID;

/**
 * 
 * UuidIdGenerator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class UuidIdGenerator implements IdGenerator {

	@Override
	public String generateTransactionId() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	@Override
	public String generateXaTransactionId() {
		return "XA-" + UUID.randomUUID().toString().replace("-", "");
	}

}
