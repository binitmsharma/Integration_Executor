package com.cipher.cloud.exceptions;

public class BrowserNotSpecifiedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -208243796375634983L;

	public BrowserNotSpecifiedException(String applicationName, String module) {
		super("Broswer Name is not specified for the application, " + applicationName + ". [Sheet : " + module + "]");
	}
}
