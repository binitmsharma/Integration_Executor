package com.cipher.cloud.exceptions;

public class InputFilesNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7911998614282168197L;

	public InputFilesNotFoundException(String inputLocation) {
		super("No input files found [Location : " + inputLocation + "] [Note : Input file names should start with Test_Input_]");
	}
}
