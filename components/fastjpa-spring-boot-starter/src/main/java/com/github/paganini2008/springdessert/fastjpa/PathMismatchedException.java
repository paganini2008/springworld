package com.github.paganini2008.springdessert.fastjpa;

/**
 * 
 * PathMismatchedException
 * 
 * @author Jimmy Hoff
 * 
 */
public class PathMismatchedException extends IllegalArgumentException {

	private static final long serialVersionUID = 6298298843692196713L;

	public PathMismatchedException(String alias, String attributeName) {
		super(alias + "." + attributeName);
	}

}
