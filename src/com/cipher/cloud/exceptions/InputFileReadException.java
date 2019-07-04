package com.cipher.cloud.exceptions;

public class InputFileReadException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -87762253989018088L;

	public InputFileReadException(String inputFileLocation, Exception e) {
		super("There was an Error in Reading the Input file [Location : "
				+ inputFileLocation + "]. Error is - " + e);
	}

	public InputFileReadException(String sheetName, int row, Exception e) {
		super("There was an Error in Reading the Input file [Sheet : "
				+ sheetName + " | Row : " + row + "]. Error is - " + e);

	}
}
