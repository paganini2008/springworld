package com.github.paganini2008.springdessert.tx;

import java.io.Serializable;

import com.github.paganini2008.devtools.beans.ToStringBuilder;
import com.github.paganini2008.devtools.beans.ToStringBuilder.PrintStyle;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * XaTransactionResponse
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Getter
@Setter
public class XaTransactionResponse implements Serializable {

	private static final long serialVersionUID = 8610702260165090624L;

	private String xaId;
	private String id;
	private boolean ok;
	private boolean completed;
	private String[] reason;
	private long elapsedTime;

	public String toString() {
		return ToStringBuilder.reflectionToString(this, PrintStyle.MULTI_LINE);
	}

}
