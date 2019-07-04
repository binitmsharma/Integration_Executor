package com.cipher.cloud.exceptions;

public class ObjectRepositoryNotInitializedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8664078596465399219L;

	public ObjectRepositoryNotInitializedException() {
		super("Object repositiry not initialized properly. "
				+ "Exception in getting physical component ID");
	}
}
