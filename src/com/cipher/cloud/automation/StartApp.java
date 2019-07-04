package com.cipher.cloud.automation;

import java.awt.EventQueue;

import com.cipher.cloud.ui.UserInterface;

public class StartApp {
	public static void main(String[] args) {

		// args = new String[1];
		// args[0]="LAB1";

		if (args.length != 0) {
			//String inputfilePath = "C:/Selenium/Input Files of Selenium testing/" + args[0];
			String inputfilePath = args[0];
			//String outputFilePath = "C:/Selenium/Input Files of Selenium testing/" + args[0];
			String outputFilePath = args[1];
			//String objectRepostitoryLocation = "C:/Selenium/Input Files of Selenium testing/" + args[0] + "/ObjectRepository.xlsx";
			String objectRepostitoryLocation = args[2];

			if (UseSelenium.setConfiguration(inputfilePath, outputFilePath, objectRepostitoryLocation).equals(""))
				UseSelenium.runSelenium(inputfilePath);
		} else {
			EventQueue.invokeLater(new Runnable() {
				
				public void run() {
					UserInterface uiInstance = UserInterface.getInstance();
					uiInstance.setVisible(true);
				}
			});
		}
	}
}
