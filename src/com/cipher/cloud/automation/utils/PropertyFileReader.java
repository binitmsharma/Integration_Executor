package com.cipher.cloud.automation.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.cipher.cloud.automation.UseSelenium;

public class PropertyFileReader {
	
	/**
	 * This method will get the configuration property file from given location.  
	 * 
	 * @param location
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void setProperties(String location) throws NumberFormatException, IOException {
		BufferedReader br = null;

		String sCurrentLine;
		br = new BufferedReader(new FileReader(location));

		while ((sCurrentLine = br.readLine()) != null) {
			String[] property = sCurrentLine.split("=");
			if (property[0].equals("generateHTMLReport")) {
				UseSelenium.generateHTMLReport = Boolean.parseBoolean(property[1]);
			} else if (property[0].equals("dynamicComponentHandlingWait")) {
				UseSelenium.dynamicComponentHandlingWait = Long.parseLong(property[1]);
			} else if(property[0].equalsIgnoreCase("orgUsername")){
				UseSelenium.orgUsername = property[1];
			} else if(property[0].equalsIgnoreCase("orgPassword")){
				UseSelenium.orgPassword = property[1];
			}
		}
		br.close();
	}
}
