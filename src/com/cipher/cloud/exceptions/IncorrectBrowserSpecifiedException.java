package com.cipher.cloud.exceptions;

public class IncorrectBrowserSpecifiedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -208243796375634983L;

	public IncorrectBrowserSpecifiedException(String applicationName, String module, String browser) {
		super("Broswer specified for the application, " + applicationName + ", is not supported or is wrong. [Sheet : " + module
				+ " | Specified browser : " + browser + "]");
	}
}
