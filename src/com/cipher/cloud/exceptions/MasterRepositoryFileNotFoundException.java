package com.cipher.cloud.exceptions;

public class MasterRepositoryFileNotFoundException extends Exception{
	
	private static final long serialVersionUID = 7921958614284168197L;
	
	public MasterRepositoryFileNotFoundException(String inputLocation) {
		super("No Master Repository file found [Location : " + inputLocation + "] [Note : Master Repository file names should start with MasterRepository]");
	}

}
