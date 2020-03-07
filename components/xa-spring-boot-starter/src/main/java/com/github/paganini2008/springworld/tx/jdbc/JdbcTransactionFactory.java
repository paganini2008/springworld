package com.github.paganini2008.springworld.tx.jdbc;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.db4j.SqlPlus;
import com.github.paganini2008.devtools.db4j.Transaction;
import com.github.paganini2008.devtools.db4j.TransactionException;

/**
 * 
 * JdbcTransactionFactory
 *
 * @author Fred Feng
 * @version 1.0
 */
public class JdbcTransactionFactory implements TransactionFactory {

	@Autowired
	private SqlPlus sqlPlus;

	@Autowired
	private TransactionEventListener transactionEventListener;

	@Override
	public JdbcTransaction createTransaction(String id) {
		Transaction transaction;
		try {
			transaction = sqlPlus.beginTransaction();
		} catch (SQLException e) {
			throw new TransactionException(e);
		}
		return new JdbcTransactionImpl(id, transaction, transactionEventListener);
	}

}
