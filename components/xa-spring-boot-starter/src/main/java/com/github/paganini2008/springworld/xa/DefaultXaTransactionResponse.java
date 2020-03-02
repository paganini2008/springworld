package com.github.paganini2008.springworld.xa;

import java.io.Serializable;

import com.github.paganini2008.devtools.beans.ToStringBuilder;
import com.github.paganini2008.devtools.beans.ToStringBuilder.PrintStyle;

/**
 * 
 * DefaultXaTransactionResponse
 *
 * @author Fred Feng
 * @version 1.0
 */
public class DefaultXaTransactionResponse implements Serializable, XaTransactionResponse {

	private static final long serialVersionUID = 8610702260165090624L;

	private String xaId;
	private String id;
	private boolean ok;
	private boolean completed;
	private String[] reason;
	private long elapsedTime;

	public DefaultXaTransactionResponse() {
	}

	DefaultXaTransactionResponse(String xaId, String id, boolean ok) {
		this.xaId = xaId;
		this.id = id;
		this.ok = ok;
	}

	public String getXaId() {
		return xaId;
	}

	public String getId() {
		return id;
	}

	public boolean isOk() {
		return ok;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public String[] getReason() {
		return reason;
	}

	public void setReason(String[] reason) {
		this.reason = reason;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean isCompleted() {
		return completed;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, PrintStyle.MULTI_LINE);
	}

	public static DefaultXaTransactionResponse commit(String xaId, String id) {
		return new DefaultXaTransactionResponse(xaId, id, true);
	}

	public static DefaultXaTransactionResponse rollback(String xaId, String id) {
		return new DefaultXaTransactionResponse(xaId, id, false);
	}

}
