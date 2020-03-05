package com.github.paganini2008.springworld.xa;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.db4j.SqlPlus;
import com.github.paganini2008.devtools.db4j.Transaction;

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

	@Override
	public XaTransaction createTransaction(String xaId) {
		Transaction transaction;
		try {
			transaction = sqlPlus.beginTransaction();
		} catch (SQLException e) {
			throw new XaTransactionException(e);
		}
		return new JdbcXaTransaction(xaId, transaction);
	}

}
