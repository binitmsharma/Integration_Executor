package com.cipher.cloud.exceptions;

public class ObjectRepositoryReadException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -87762253989018088L;

	public ObjectRepositoryReadException(String objectRepositoryLocation,
			Exception e) {
		super(
				"There was an Error in Reading the Object Repository file [File : "
						+ objectRepositoryLocation + "]. Error is - " + e);
	}

	public ObjectRepositoryReadException(String objectRepositoryLocation,
			String sheetName, int row, Exception e) {
		super(
				"There was an Error in Reading the Object Repository file [File : "
						+ objectRepositoryLocation + " |Sheet : " + sheetName
						+ " | row : " + row + "]. Error is - " + e);

	}
}
