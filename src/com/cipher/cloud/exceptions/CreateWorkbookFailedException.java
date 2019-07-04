package com.cipher.cloud.exceptions;

public class CreateWorkbookFailedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8971545447566745616L;

	public CreateWorkbookFailedException(Exception e) {
		super("Create workbook failed because of the following error - " + e);
	}
}
