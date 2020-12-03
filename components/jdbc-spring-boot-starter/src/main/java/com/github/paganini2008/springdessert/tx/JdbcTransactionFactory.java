package com.github.paganini2008.springdessert.tx;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.db4j.SqlPlus;
import com.github.paganini2008.devtools.db4j.Transaction;
import com.github.paganini2008.devtools.db4j.TransactionException;

/**
 * 
 * JdbcTransactionFactory
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class JdbcTransactionFactory implements TransactionFactory {

	@Autowired
	private SqlPlus sqlPlus;

	@Autowired
	private TransactionEventPublisher transactionEventPublisher;

	@Override
	public JdbcTransaction newTransaction(String id) {
		Transaction transaction;
		try {
			transaction = sqlPlus.beginTransaction();
		} catch (SQLException e) {
			throw new TransactionException(e);
		}
		return new JdbcTransaction(id, transaction, transactionEventPublisher);
	}

}
