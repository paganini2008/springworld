package com.github.paganini2008.springworld.tx;

import java.util.UUID;

/**
 * 
 * DefaultTransactionId
 *
 * @author Fred Feng
 * @version 1.0
 */
public class DefaultTransactionId implements TransactionId {

	@Override
	public String generateId() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	@Override
	public String generateXaId() {
		return "XA-" + UUID.randomUUID().toString().replace("-", "");
	}

}
