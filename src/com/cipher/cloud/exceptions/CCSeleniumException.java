package com.cipher.cloud.exceptions;

public class CCSeleniumException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1316255459170021282L;

	public CCSeleniumException(String message) {
		super(message);
	}

	public CCSeleniumException(Exception e) {
		super("The script threw the following exception - " + e);
	}
}
