package com.github.paganini2008.springworld.tx;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.db4j.SqlPlus;
import com.github.paganini2008.devtools.db4j.Transaction;
import com.github.paganini2008.devtools.db4j.TransactionException;

/**
 * 
 * JdbcXaTransactionFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public class JdbcXaTransactionFactory implements XaTransactionFactory {

	@Autowired
	private SqlPlus sqlPlus;

	@Autowired
	private TransactionEventPublisher transactionEventPublisher;

	@Autowired
	private IdGenerator idGenerator;

	@Override
	public XaTransaction newTransaction(String xaId) {
		Transaction transaction;
		try {
			transaction = sqlPlus.beginTransaction();
		} catch (SQLException e) {
			throw new TransactionException(e);
		}
		return new JdbcXaTransaction(xaId, idGenerator.generateTransactionId(), transaction, transactionEventPublisher);
	}

}
