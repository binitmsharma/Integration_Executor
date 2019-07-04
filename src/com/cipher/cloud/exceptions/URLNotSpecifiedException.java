package com.cipher.cloud.exceptions;

public class URLNotSpecifiedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -208243796375634983L;

	public URLNotSpecifiedException(String applicationName, String module) {
		super("URL is not specified for the application, " + applicationName + " [Sheet : " + module+"]");
	}
}
