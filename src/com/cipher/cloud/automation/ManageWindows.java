package com.cipher.cloud.automation;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;

import com.cipher.cloud.automation.utils.ExcelHandler;

public class ManageWindows {
	private static Logger logger = Logger.getLogger(ManageWindows.class);

	private static ArrayList<String> windowIDs = new ArrayList<String>();

	public static void storeWindow(String windowID) {
		windowIDs.add(windowID);
	}

	public static void closeOpenWindows(WebDriver driver) throws Exception {
		logger.debug("closeOpenWindows() execution started");
		for (int counter = 0; counter < windowIDs.size(); counter++) {
			closeOpenWindow(driver);
		}
		logger.debug("closeOpenWindows() execution ended");
	}

	public static void closeOpenWindow(WebDriver driver) throws Exception {
		logger.debug("closeOpenwindow() execution started");

		driver.close();
		Thread.sleep(1000 * 2);
		try {
			if (driver != null && driver.switchTo() != null) {
				// System.out.println("Before alert get");
				Alert a = driver.switchTo().alert();
				// System.out.println("Post getting alert");
				if (a != null) {
					a.accept();
				}
			}
		} catch (Exception e) {
			logger.error("Exception occurred while closing the window. Error is - "
					+ e);
		}
		if (windowIDs.size() > 0) {
			driver.switchTo().window(windowIDs.get(windowIDs.size() - 1));
			driver.switchTo().window(windowIDs.get(windowIDs.size() - 1));
			windowIDs.remove(windowIDs.size() - 1);
		}
		logger.debug("closeOpenwindow() execution ended");
	}

	public static int handleWindowAuthentication(XSSFWorkbook book, WebDriver driver) {
		try {
			driver.switchTo().alert();
		} catch (Exception e) {
			// System.out.println("there is no authorisation required");
			return -1;
		}
		// Code to handle Basic Browser Authentication in Selenium.
		// System.out.println("In handleWindowAuthentication");
		Alert aa = driver.switchTo().alert();
		try {
			String[] userCredentials = getUserIdAndPassword(book);
			System.out.println(userCredentials[0] + userCredentials[1]);
			aa.sendKeys(userCredentials[0]);
			Robot a = new Robot();
			a.keyPress(KeyEvent.VK_ENTER);
			a.keyPress(KeyEvent.VK_ENTER);
			Thread.sleep(5000);
			aa.sendKeys(userCredentials[1]);
			a.keyPress(KeyEvent.VK_ENTER);
			a.keyPress(KeyEvent.VK_ENTER);
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	private static String[] getUserIdAndPassword(XSSFWorkbook book)
			throws Exception {
		String[] credentials = new String[2];
		credentials[0] = ExcelHandler.getCellValueX(book, "Sheet1", 0, 7);
		credentials[1] = ExcelHandler.getCellValueX(book, "Sheet1", 1, 7);
		return credentials;
	}
}
