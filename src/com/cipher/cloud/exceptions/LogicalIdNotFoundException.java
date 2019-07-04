package com.cipher.cloud.exceptions;

public class LogicalIdNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6394475715397541776L;

	public LogicalIdNotFoundException(String logicalId) {
		super("Unable to find Logical Id, " + logicalId
				+ ", in the Object Repository");
	}
}
