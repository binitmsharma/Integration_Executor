package com.cipher.cloud.exceptions;

public class SheetNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1186106076508351481L;

	public SheetNotFoundException(String sheetName) {
		super("Sheet, " + sheetName + ", is not found.");
	}

	public SheetNotFoundException(int sheetNumber) {
		super("Sheet " + sheetNumber + " is not found.");
	}
}
