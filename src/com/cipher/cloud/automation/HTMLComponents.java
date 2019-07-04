package com.cipher.cloud.automation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cipher.cloud.automation.utils.ExcelHandler;
import com.cipher.cloud.automation.utils.ExcelRow;
import com.cipher.cloud.automation.utils.MiscellaneousUtil;
import com.cipher.cloud.exceptions.CCSeleniumException;
import com.cipher.cloud.exceptions.ElementNotFoundException;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

public class HTMLComponents {

	private static Logger logger = Logger.getLogger(HTMLComponents.class);

	public static String copyValue; 
	private static final int BUFFER_SIZE = 4096;
	/**
	 * @author binit.mohan
	 * 
	 * 
	 * 
	 * 
	 * @param selenium
	 * @param outPutWorkBookx
	 * @param excelRow
	 * @param dynamicID
	 * @param paging
	 * @param driver
	 * @param module
	 * @return
	 * @throws Exception
	 */
	public static int handleDynamicComponent(Selenium selenium, XSSFWorkbook outPutWorkBookx, int excelRow, int dynamicID,
			int paging, WebDriver driver, String module) throws Exception {

		logger.debug("handleDynamicComponent() execution started");

		String componentID, componentValue, componentType, tmpCompID, pageComponentValue = null;
		componentID = ExcelHandler.getCellValueX(outPutWorkBookx, module, excelRow, 3);
		componentID = ObjectRepository.getElementFromhashmap(componentID);
		componentType = ExcelHandler.getCellValueX(outPutWorkBookx, module, excelRow, 4);
		componentValue = ExcelHandler.getCellValueX(outPutWorkBookx, module, excelRow, 5);
		if (dynamicID < 0)
			dynamicID = 0;
		if (componentID != null && !componentID.trim().equals("")) {
			tmpCompID = componentID;
			for (int counter = dynamicID;; counter++) {
				tmpCompID = componentID;
				//tmpCompID = tmpCompID.replace("xx", "" + counter + "");
				if ((findElement(driver, tmpCompID)) == null) {
					if (counter >= 2) {
						logger.error("Element = " + tmpCompID + " not present on the page");
						logger.debug("handleDynamicComponent() execution ended");
						return -2;
					} else
						counter++;
				} else if (componentType.contains("label")) {
					pageComponentValue = selenium.getText(tmpCompID);
				} else if (componentType.contains("value")) {
					pageComponentValue = (selenium.getSelectedLabel(tmpCompID)).trim();
				} else if (componentType.contains("checkbox")) {
					selenium.focus(tmpCompID);
					WebElement thisElement = findElement(driver, tmpCompID);
					if (thisElement.isSelected())
						pageComponentValue = "true";
					else
						pageComponentValue = "false";

				} else if (componentType.contains("text")) {
					pageComponentValue = selenium.getValue(tmpCompID);
				}
				if (pageComponentValue != null && pageComponentValue.equalsIgnoreCase(componentValue)) {
					logger.debug("handleDynamicComponent() execution ended");
					logger.debug("handleDynamicComponent() - Dynamic id identified : " + counter);
					return counter;
				}
			}
		}
		logger.debug("handleDynamicComponent() execution ended");
		return -1;
	}
	/**
	 * @author binit.mohan
	 * 
	 * @param selenium
	 * @param outPutWorkBookx
	 * @param excelRow
	 * @param dynamicID
	 * @param paging
	 * @param driver
	 * @param module
	 * @return
	 * @throws Exception
	 */
	public static int handleDynamicComponent(Selenium selenium, String componentID, String componentType, String componentValue, int success, WebDriver driver, String module) throws Exception {

		logger.debug("handleDynamicComponent() execution started");

		String pageComponentValue = null;
		WebElement thisElement = null;
		
		if (success < 0 && componentID != null && !componentID.trim().equals("")) {
			thisElement = findElement(driver, componentID);
			if (thisElement == null) {
						logger.error("Element = " + componentID + " not present on the page");
						logger.debug("handleDynamicComponent() execution ended");
						return -1;
					} 
						
				 else if (componentType.contains("label")) {
					pageComponentValue = selenium.getText(componentID);
				} else if (componentType.contains("value")) {
					pageComponentValue = (selenium.getSelectedLabel(componentID)).trim();
				} else if (componentType.contains("checkbox")) {
					selenium.focus(componentID);
					
					if (thisElement.isSelected())
						pageComponentValue = "true";
					else
						pageComponentValue = "false";

				} else if (componentType.contains("text")) {
					pageComponentValue = selenium.getValue(componentID);
				}
				if (pageComponentValue != null) {
					logger.debug("handleDynamicComponent() execution ended");
					return 1;
				}
			
		}
		logger.debug("handleDynamicComponent() execution ended");
		return -1;
	
	}


	/**
	 * 
	 * 
	 * @param selenium
	 * @param outPutWorkBookx
	 * @param excelRow
	 * @param driver
	 * @param browser
	 * @param componentID
	 * @param componentType
	 * @param module
	 * @throws Exception
	 */

	public static void handleDynamicComponentClick(Selenium selenium, XSSFWorkbook outPutWorkBookx, int excelRow, WebDriver driver,
			String browser, String componentID, String componentType, String module) throws Exception {

		logger.debug("handleDynamicComponentClick() execution started");
		String componentValue = ExcelHandler.getCellValueX(outPutWorkBookx, module, excelRow, 5);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		if (componentType.contains("checkbox")) {

			if (componentValue.equalsIgnoreCase("false")) {
				selenium.focus(componentID);
				WebElement thisElement = findElement(driver, componentID);

				if (thisElement.isSelected())
					fireClick(thisElement, browser);

			} else if (componentValue.equalsIgnoreCase("true")) {

				selenium.focus(componentID);
				WebElement thisElement = findElement(driver, componentID);

				if (!thisElement.isSelected())
					fireClick(thisElement, browser);
			} else {
				selenium.focus(componentID);
				WebElement thisElement = findElement(driver, componentID);
				fireClick(thisElement, browser);
			}

		} else if (componentType.contains("text")) {
			// componentValue = ExcelHandler.getCellValueX(outPutWorkBookx,
			// module, excelRow, 5); //getCellValueX(excelSheetx, excelRow, 5);
			if (componentValue != null && componentValue != "")
				selenium.type(componentID, componentValue);

		} else if (componentType.contains("select-index")) {
			// componentValue = ExcelHandler.getCellValueX(outPutWorkBookx,
			// module, excelRow, 5); //getCellValueX(excelSheetx, excelRow, 5);
			selenium.select(componentID, "index=" + Integer.parseInt(componentValue));

		} else if (componentType.contains("select-value")) {
			// componentValue = getCellValueX(excelSheetx, excelRow, 5);
			selenium.select(componentID, "label=" + componentValue);

		} else if (componentType.contains("radio")) {

			// componentValue = getCellValueX(excelSheetx, excelRow, 5);
			componentID = componentID.substring(3);
			if (componentValue == null || componentValue.equals(""))
				componentValue = "0";
			WebElement query_enquirymode = driver.findElements(By.id(componentID)).get(Integer.parseInt(componentValue));
			selenium.focus(componentID);
			fireClick(query_enquirymode, browser);

		} else if (componentType.contains("button")) {
			selenium.focus(componentID);
			WebElement thisElement = findElement(driver, componentID);
			fireClick(thisElement, browser);

		} else if (componentType.contains("href")) {
			selenium.focus(componentID);
			if (componentID.startsWith("link")) {
				selenium.click(componentID);
			} else {
				WebElement thisElement = findElement(driver, componentID);
				fireClick(thisElement, browser);
			}
		} /*
		 * else if (componentType.contains("hrefe")) {
		 * selenium.focus(componentID); if (componentID.startsWith("link")) {
		 * selenium.click(componentID); } else { WebElement thisElement =
		 * findElement(driver, componentID); JavascriptExecutor executor =
		 * (JavascriptExecutor)driver;
		 * executor.executeScript("arguments[0].click();", thisElement);
		 * //fireClick(thisElement, browser); } }
		 */else if (componentType.contains("Image")) {
			selenium.focus(componentID);
			WebElement thisElement = findElement(driver, componentID);
			fireClick(thisElement, browser);

		}
		logger.debug("handleDynamicComponentClick() execution ended");
	}
	
	/**
	 * 
	 * @author binit.mohan
	 * 
	 * @param selenium
	 * @param outPutWorkBookx
	 * @param excelRow
	 * @param driver
	 * @param browser
	 * @param componentID
	 * @param componentType
	 * @param module
	 * @throws Exception
	 */

	public static void handleDynamicComponentClick(Selenium selenium, XSSFWorkbook outPutWorkBookx, int excelRow, WebDriver driver,
			String browser, String componentID, String componentType, String module, WebElement thisElement) throws Exception {

		logger.debug("handleDynamicComponentClick() execution started");
		String componentValue = ExcelHandler.getCellValueX(outPutWorkBookx, module, excelRow, 5);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		if (componentType.contains("checkbox")) {

			if (componentValue.equalsIgnoreCase("false")) {
				selenium.focus(componentID);
				

				if (thisElement.isSelected())
					fireClick(thisElement, browser);

			} else if (componentValue.equalsIgnoreCase("true")) {

				selenium.focus(componentID);
				

				if (!thisElement.isSelected())
					fireClick(thisElement, browser);
			} else {
				selenium.focus(componentID);
				
				fireClick(thisElement, browser);
			}

		} else if (componentType.contains("text")) {
			// componentValue = ExcelHandler.getCellValueX(outPutWorkBookx,
			// module, excelRow, 5); //getCellValueX(excelSheetx, excelRow, 5);
			if (componentValue != null && componentValue != "")
				selenium.type(componentID, componentValue);

		} else if (componentType.contains("select-index")) {
			// componentValue = ExcelHandler.getCellValueX(outPutWorkBookx,
			// module, excelRow, 5); //getCellValueX(excelSheetx, excelRow, 5);
			selenium.select(componentID, "index=" + Integer.parseInt(componentValue));

		} else if (componentType.contains("select-value")) {
			// componentValue = getCellValueX(excelSheetx, excelRow, 5);
			selenium.select(componentID, "label=" + componentValue);

		} else if (componentType.contains("radio")) {

			// componentValue = getCellValueX(excelSheetx, excelRow, 5);
			componentID = componentID.substring(3);
			if (componentValue == null || componentValue.equals(""))
				componentValue = "0";
			WebElement query_enquirymode = driver.findElements(By.id(componentID)).get(Integer.parseInt(componentValue));
			selenium.focus(componentID);
			fireClick(query_enquirymode, browser);

		} else if (componentType.contains("button")) {
			selenium.focus(componentID);
			fireClick(thisElement, browser);

		} else if (componentType.contains("click")) {
			selenium.focus(componentID);
			if (componentID.startsWith("link")) {
				selenium.click(componentID);
			} else {
				fireClick(thisElement, browser);
			}
		} /*
		 * else if (componentType.contains("hrefe")) {
		 * selenium.focus(componentID); if (componentID.startsWith("link")) {
		 * selenium.click(componentID); } else { WebElement thisElement =
		 * findElement(driver, componentID); JavascriptExecutor executor =
		 * (JavascriptExecutor)driver;
		 * executor.executeScript("arguments[0].click();", thisElement);
		 * //fireClick(thisElement, browser); } }
		 */else if (componentType.contains("Image")) {
			selenium.focus(componentID);
			fireClick(thisElement, browser);

		}
		 else if(componentType.contains("alert")){
			 driver.switchTo().alert().accept();
		 }
		logger.debug("handleDynamicComponentClick() execution ended");
	}

	/**
	 * @author binit.mohan
	 * 
	 *         This method find the browser element based on their id, name,
	 *         link, class and Xpath.
	 * 
	 * @param driver
	 * @param element
	 * @return
	 * 
	 */
	public static WebElement findElement(WebDriver driver, String element) {
		driver.switchTo();
		WebElement ele = null;
		By by = null;
		if (element.startsWith("id="))
			by = By.id(element.substring(3));
		else if (element.startsWith("name="))
			by = By.name(element.substring(5));
		else if (element.startsWith("link="))
			by = By.linkText(element.substring(5));
		else if (element.startsWith("class="))
			by = By.className(element.substring(6));
		else
			by = By.xpath(element);

		try {
			ele = driver.findElement(by);
		} catch (Exception e) {
			logger.error("Exception occurred in findElement method in HTMLComponents Class for element : " + element, e);
		}

		return ele;

	}

	/**
	 * @author binit.mohan
	 * 
	 *         This method Added to handle radio buttons.
	 * 
	 * @param driver
	 * @param element
	 * @return
	 * 
	 */
	public static List<WebElement> findlistOfElements(WebDriver driver, String element) {
		driver.switchTo();

		List<WebElement> eleList = null;
		By by = null;
		if (element.startsWith("id="))
			by = By.id(element.substring(3));
		else if (element.startsWith("name="))
			by = By.name(element.substring(5));
		else if (element.startsWith("link="))
			by = By.linkText(element.substring(5));
		else if (element.startsWith("class="))
			by = By.className(element.substring(6));
		else
			by = By.xpath(element);

		try {
			eleList = driver.findElements(by);
		} catch (Exception e) {
			logger.error("Exception occurred in findElement method in HTMLComponents Class for element : " + element, e);
		}

		return eleList;

	}
	
	/**
	 * 
	 * @param driver
	 * @param element
	 * @return
	 */
	public static By findElementBy(WebDriver driver, String element) {
		By by = null;
		if (element.startsWith("id="))
			by = By.id(element.substring(3));
		else if (element.startsWith("name="))
			by = By.name(element.substring(5));
		else if (element.startsWith("link="))
			by = By.linkText(element.substring(5));
		else if (element.startsWith("class="))
			by = By.className(element.substring(6));
		else
			by = By.xpath(element);

		
		return by;

	}

	/**
	 * 
	 * 
	 * @param element
	 * @param browser
	 */

	public static void fireClick(WebElement element, String browser) {
		if (browser.equalsIgnoreCase("IE")) {
			element.sendKeys(Keys.CONTROL);
			element.click();
		} else
			element.click();
	}

	/**
	 * @author binit.mohan
	 * 
	 * 
	 *         This method will handle the browser operations.
	 * 
	 * @param selenium
	 * @param outPutWorkBookx
	 * @param excelRow
	 * @param dynamicID
	 * @param paging
	 * @param driver
	 * @param module
	 * @return
	 * @throws Exception
	 */
	public static boolean generalHTMLElementHandling(boolean menuClick,
			String browser, boolean validate, boolean encryptionCheck,
			WebDriver driver, String componentID, String componentType,
			String componentValue, Selenium selenium,
			XSSFWorkbook inPutWorkBookx, int excelRow, String module)
			throws CCSeleniumException {
		// StepResult stepResult = new StepResult();
		// boolean stepResult = true;
		String captureLable = null;
		Actions actions = new Actions(driver);

		try {
			// Code to handle checkbox
			String logicalComponentID = ExcelHandler.getCellValueX(inPutWorkBookx, module, excelRow, 3);
			if (componentType.equalsIgnoreCase("checkbox")) {
				if (componentValue.equalsIgnoreCase("false")) {
					// selenium.focus(componentID);
					WebElement thisElement = findElement(driver, componentID);
					if (thisElement != null) {
						if (thisElement.isSelected())
							fireClick(thisElement, browser);
					} else {
						logger.error("Unable to find element " + logicalComponentID);
						logger.debug("Unable to find element " + logicalComponentID);
						throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
					}
				} else if (componentValue.equalsIgnoreCase("true")) {
					// selenium.focus(componentID);
					WebElement thisElement = findElement(driver, componentID);

					if (thisElement != null) {
						if (!thisElement.isSelected())
							fireClick(thisElement, browser);
					} else {
						logger.error("Unable to find element " + logicalComponentID);
						logger.debug("Unable to find element " + logicalComponentID);
						throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
					}
				} else {
					// selenium.focus(componentID);
					if (componentValue == null || componentValue.equals(""))
						componentValue = "0";
					WebElement query_enquirymode = findlistOfElements(driver, componentID).get(Integer.parseInt(componentValue));
					// selenium.focus(componentID);
					if (query_enquirymode != null) {
						fireClick(query_enquirymode, browser);
					} else {
						logger.error("Unable to find element " + logicalComponentID);
						logger.debug("Unable to find element " + logicalComponentID);
						throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
					}

					/*WebElement thisElement = findElement(driver, componentID); 
					  fireClick(thisElement, browser);*/

				}

			}
			// Code to handle Text
			else if (componentType.equalsIgnoreCase("text")) {
				if (validate) {
					componentValue = selenium.getValue(componentID); // need to have some action if fails,
																		// catch
																		// is
																		// not
																		// just
																		// enough
				}
				if (componentValue != null && componentValue != "" && selenium != null) {
					selenium.type(componentID, componentValue);
				} else {
					WebElement thisElement = findElement(driver, componentID);
					if (thisElement != null) {
						thisElement.sendKeys(componentValue);
					} else {
						logger.error("Unable to find element " + logicalComponentID);
						logger.debug("Unable to find element " + logicalComponentID);
						throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
					}
				}
			}

			// Code to handle label
			else if (componentType.equalsIgnoreCase("label")) {
				if (validate) {
					captureLable = selenium.getText(componentID);
				}
				/*if (captureLable != null && captureLable != "" && selenium != null) {
					// selenium.type(componentID, componentValue); /// no point
					// of typing anything in label
				}*/
			}

			// code to handle ---
			else if (componentType.equalsIgnoreCase("select-index")) {
				if (validate || componentValue.equals("")) {
					componentValue = (selenium.getSelectedLabel(componentID)).trim();
					// where in the reports this component value
									// is displaying
				} else {
					// selenium.select(componentID,"index=" +
					// Integer.parseInt(componentValue));
					WebElement thisElement = findElement(driver, componentID);
					if (thisElement != null) {
						Select sel = new Select(thisElement);
						sel.selectByIndex(Integer.parseInt(componentValue));
					} else {
						logger.error("Unable to find element " + logicalComponentID);
						logger.debug("Unable to find element " + logicalComponentID);
						throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
					}
				}
			}

			// code to handle dropdowns
			else if (componentType.equalsIgnoreCase("select-value")) {

				if (validate || componentValue.equals("")) {
					componentValue = (selenium.getSelectedLabel(componentID)).trim();

				}

				else {
					// selenium.select(componentID, "label=" + componentValue);
					WebElement thisElement = findElement(driver, componentID);
					if (thisElement != null) {
						Select sel = new Select(thisElement);
						sel.selectByVisibleText(componentValue);
					} else {
						logger.error("Unable to find element " + logicalComponentID);
						logger.debug("Unable to find element " + logicalComponentID);
						throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
					}
				}
			}

			// code to handle radio button --updated by @Suruchi
			else if (componentType.equalsIgnoreCase("radio")) {
				// componentValue = getCellValueX(excelSheetx, excelRow, 5);
				if (componentValue == null || componentValue.equals(""))
					componentValue = "0";
				WebElement query_enquirymode = findlistOfElements(driver, componentID).get(Integer.parseInt(componentValue));
				// selenium.focus(componentID);
				if (query_enquirymode != null) {
					fireClick(query_enquirymode, browser);
				} else {
					logger.error("Unable to find element " + logicalComponentID);
					logger.debug("Unable to find element " + logicalComponentID);
					throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
				}

			}

			// code to handle buttons
			else if (componentType.equalsIgnoreCase("button")) {
				// selenium.focus(componentID);
				WebElement thisElement = findElement(driver, componentID);
				if (thisElement != null) {
					fireClick(thisElement, browser);
				} else {
					logger.error("Unable to find element " + logicalComponentID);
					logger.debug("Unable to find element " + logicalComponentID);
					throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
				}
			}
			
			else if (componentType.equalsIgnoreCase("Copy")) {
				// selenium.focus(componentID);
				WebElement thisElement = findElement(driver, componentID);
				if (thisElement != null) {
					copyValue = thisElement.getText();
					
				} else {
					logger.error("Unable to find element " + logicalComponentID);
					logger.debug("Unable to find element " + logicalComponentID);
					throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
				}
			}
			else if (componentType.equalsIgnoreCase("Paste")) {
				// selenium.focus(componentID);
				WebElement thisElement = findElement(driver, componentID);
				if (thisElement != null) {
					thisElement.sendKeys(copyValue);
					
				} else {
					logger.error("Unable to find element " + logicalComponentID);
					logger.debug("Unable to find element " + logicalComponentID);
					throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
				}
			}

			// code to handle Links
			/*
			 * else if (componentType.equalsIgnoreCase("hrefe")) {
			 * //selenium.focus(componentID); if
			 * (componentID.startsWith("link")) { selenium.click(componentID); }
			 * else { WebElement thisElement = findElement(driver, componentID);
			 * if(thisElement != null){ JavascriptExecutor executor =
			 * (JavascriptExecutor)driver;
			 * executor.executeScript("arguments[0].click();", thisElement);
			 * //fireClick(thisElement, browser); } else {
			 * logger.error("Unable to find element " + logicalComponentID);
			 * logger.debug("Unable to find element " + logicalComponentID);
			 * throw new CCSeleniumException("Unable to find element - " +
			 * logicalComponentID); } } }
			 */
			else if (componentType.equalsIgnoreCase("href")) {
				// selenium.focus(componentID);
				if (componentID.startsWith("link")) {
					selenium.click(componentID);
				} else {
					WebElement thisElement = findElement(driver, componentID);
					if (thisElement != null) {
						fireClick(thisElement, browser);
					} else {
						logger.error("Unable to find element " + logicalComponentID);
						logger.debug("Unable to find element " + logicalComponentID);
						throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
					}
				}
			}
			// code to handle images
			else if (componentType.equalsIgnoreCase("Image")) {
				// selenium.focus(componentID);
				WebElement thisElement = findElement(driver, componentID);
				if (thisElement != null) {
					fireClick(thisElement, browser);
				} else {
					logger.error("Unable to find element " + logicalComponentID);
					logger.debug("Unable to find element " + logicalComponentID);
					throw new CCSeleniumException("Unable to find element - " + logicalComponentID);
				}
			}

			// code to handle Menu
			else if (componentType.equalsIgnoreCase("menu")) {
				WebElement acM = null;
				// acM = driver.findElement(By.linkText(componentID));
				acM = findElement(driver, componentID);
				if (acM != null) {
					actions.moveToElement(acM);
					if (menuClick) {
						actions.click();
					}
					actions.perform();
				} else {
					logger.error("Unable to find element " + logicalComponentID);
					logger.debug("Unable to find element " + logicalComponentID);
					throw new CCSeleniumException("Unable to find element - "+ logicalComponentID);
				}
			}

			// code to handle Frameout
			else if (componentType.equalsIgnoreCase("Frameout")) {
				try {
					driver.switchTo().defaultContent();
				} catch (org.openqa.selenium.NoSuchFrameException nsfe) {
					logger.error("No Frame Error occurred. Error is - " + nsfe.getMessage(), nsfe);
				}
			}

			// code for switch to open window
			else if (componentType.equalsIgnoreCase("SwithToOpenWindow")) {
				String loc = ExcelHandler.getCellValueX(inPutWorkBookx, module, excelRow, 5);

				ManageWindows.storeWindow(driver.getWindowHandle());

				// Mitul - A Small wait to open the window
				Thread.sleep(UseSelenium.dynamicComponentHandlingWait);
				// binit.mohan Check whether new window page has title or not.
				if (!loc.isEmpty() && selenium != null) {
					selenium.selectWindow(loc);
					driver = ((WebDriverBackedSelenium) selenium).getWrappedDriver();
					actions = new Actions(driver);
					// driver = driver;
					driver.switchTo().defaultContent();
				}

				else {
					for (String winHandle : driver.getWindowHandles()) {
						driver.switchTo().window(winHandle); // switch focus of
																// WebDriver to
																// the next
																// found window
																// handle
																// (that's your
																// newly opened
																// window)
						driver.manage().window().maximize();
					}

				}
			}

			/*
			 * //code to upload files else
			 * if(componentType.equalsIgnoreCase("UploadFile")) { WebElement
			 * fileInput = driver.findElement(By.id(componentID.substring(3)));
			 * // need to be generalize fileInput.sendKeys(componentValue); }
			 */

			// code to upload files
			else if (componentType.equalsIgnoreCase("UploadFile")) {
				WebElement fileInput = findElement(driver, componentID);
				fileInput.sendKeys(new CharSequence[] { componentValue });
			}
			
			else if (componentType.equalsIgnoreCase("PDFFileCompare")) {
				
				boolean res = MiscellaneousUtil.comparePDFFiles(MiscellaneousUtil.lastFileModified(UseSelenium.outputLocation), componentValue);
				
				if(!res){
					throw new CCSeleniumException("PDF files not similar");
				}
			}
			
			else if (componentType.equalsIgnoreCase("ReadExcelValue")) {
				
				boolean res = ExcelHandler.readExcelValue(MiscellaneousUtil.lastFileModified(UseSelenium.outputLocation), excelRow);
				
				if(!res){
					throw new CCSeleniumException("Row is empty");
				}
			}
			else if (componentType.equalsIgnoreCase("ExcelFileCompare")) {


				try {
					FileInputStream downloadedExcelFile1 = new FileInputStream(new File(MiscellaneousUtil.lastFileModified(UseSelenium.outputLocation)));
					FileInputStream baseFile = new FileInputStream(new File(componentValue));

					boolean res = ExcelHandler.compareExcel(downloadedExcelFile1, baseFile);
					if(!res){
						throw new CCSeleniumException("Excel files not similar");
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new CCSeleniumException("Excel files not similar");
				}
			}
			
			else if (componentType.equalsIgnoreCase("DoubleClick")) {
				WebElement thisElement = findElement(driver, componentID);
				if (thisElement != null) {
					actions.moveToElement(thisElement).doubleClick().perform();
				} 
			}
                

			
			// code for hover over
			else if (componentType.equalsIgnoreCase("MouseHover")) {
				// componentValue = ExcelHandler.getCellValueX(inPutWorkBookx,module, excelRow, 5);
				WebElement Elem1 = findElement(driver, componentID);
				
				if (Elem1 != null) {
					actions.clickAndHold(Elem1).build().perform();
					// WebElement myDynamicElement = (new WebDriverWait(driver,
					// 10)).until(ExpectedConditions.presenceOfElementLocated(By.id("myDynamicElement")));

				}
			}

			// code for switching to open window

			else if (componentType.equalsIgnoreCase("SwithToOpenTmpWindow")) {
				String loc = ExcelHandler.getCellValueX(inPutWorkBookx, module,
						excelRow, 5);
				// Mitul - A Small wait to open the window
				Thread.sleep(UseSelenium.dynamicComponentHandlingWait);
				// Mitul - A Small wait to open the window
				if (!loc.isEmpty() && selenium != null) {
					selenium.selectWindow(loc);
					driver = ((WebDriverBackedSelenium) selenium).getWrappedDriver();
					actions = new Actions(driver);
					// driver = driver;
					driver.switchTo().defaultContent();
				}
			}

			// Code for handling frames (added check for name and also added to find the element basis of xpath) 
			else if (componentType.equalsIgnoreCase("Frame")) {
				try {
					
					WebElement frame1 = null;
					if(componentValue == null || componentValue.equals("")){
						frame1 = findElement(driver, componentID);
					}
					else {
						frame1 = findElement(driver, componentValue);
					}
					
					if (frame1 != null) {
						// driver.switchTo().defaultContent();
						driver.switchTo().frame(frame1);
					}
				} catch (org.openqa.selenium.NoSuchFrameException nsfe) {
					logger.error("No frame Error occurred. Error is - " + nsfe.getMessage(), nsfe);
					// logger.debug("no such frame");
				}
			}
			
			else if (componentType.contains("Module")) {
				HandleSeleniumExecution handleSelExex = new HandleSeleniumExecution();
				handleSelExex.executeModuleFlow(5, componentValue, inPutWorkBookx, 1, browser);
			}
			// Code for handling force wait
			else if (componentType.contains("ForceWait")) {
				componentValue = ExcelHandler.getCellValueX(inPutWorkBookx, module, excelRow, 5);
				Thread.sleep(Long.parseLong(componentValue));
			}
			
			//Code for extract zip files 
			else if (componentType.equalsIgnoreCase("UnZip")) {
				
				MiscellaneousUtil.unZip(UseSelenium.outputLocation);
			}
			
			else if (componentType.equalsIgnoreCase("HandleAlert")) {
				if(componentValue.equalsIgnoreCase("Accept") || componentValue.isEmpty()){
					driver.switchTo().alert().accept();
				} else {
					driver.switchTo().alert().dismiss();
				}
				
			}
			
			else if (componentType.equalsIgnoreCase("Clear")) {
				WebElement thisElement = findElement(driver, componentID);
				if(thisElement != null){
					thisElement.clear();
				}
			}
			
			else if (componentType.equalsIgnoreCase("FindElementByJavaScript")){
				((JavascriptExecutor) driver).executeScript(componentValue);
				}

			// Code for handling multi select box
			else if (componentType.contains("SelectMultipelValues")) {
				componentValue = ExcelHandler.getCellValueX(inPutWorkBookx, module, excelRow, 5);
				String multipleSel[] = componentValue.split(",");

				for (String valueToBeSelected : multipleSel) {

					new Select(driver.findElement(By.id(componentID))).selectByVisibleText(valueToBeSelected);
					driver.findElement(By.id(componentID)).sendKeys(Keys.CONTROL);

				}

			} 
			// Code for handling command
			else if (componentType.contains("Command")) {
				ProcessBuilder builder = new ProcessBuilder(
						"cmd.exe",
						"/c",
						"cd \"C:\\Program Files\\Android\\android-sdk\\platform-tools\" && adb shell input keyevent 82");
				builder.redirectErrorStream(true);
				Process p = builder.start();
			}

			// / code for validation
			if (validate) {
				
				String startIndex = ExcelHandler.getCellValueX(inPutWorkBookx, module, excelRow, 8).trim();
				if (startIndex.contains("."))
					startIndex = startIndex.substring(0, startIndex.indexOf("."));
				String endIndex = ExcelHandler.getCellValueX(inPutWorkBookx, module, excelRow, 9).trim();
				if (endIndex.contains("."))
					endIndex = endIndex.substring(0, endIndex.indexOf("."));
				if (!startIndex.equals("") && !endIndex.equals("")) {
					componentValue = componentValue.substring(Integer.valueOf(startIndex) - 1, Integer.valueOf(endIndex));
				}
				String matchType = ExcelHandler.getCellValueX(inPutWorkBookx, module, excelRow, 10).trim();
				// for the validation if match type is blank
				// then it will use equals method to
				// compare.

				if (matchType.equalsIgnoreCase("contains") && !captureLable.contains(componentValue)) {
					throw new CCSeleniumException("Value mismatch - " + logicalComponentID);
				} else if (!matchType.equalsIgnoreCase("contains") && !captureLable.equals(componentValue)) {
					throw new CCSeleniumException("Value mismatch -" + logicalComponentID);
				}
			}
			if (encryptionCheck) {
				String startIndex = ExcelHandler.getCellValueX(inPutWorkBookx, module, excelRow, 8).trim();
				if (startIndex.contains("."))
					startIndex = startIndex.substring(0, startIndex.indexOf("."));
				String endIndex = ExcelHandler.getCellValueX(inPutWorkBookx, module, excelRow, 9).trim();
				if (endIndex.contains("."))
					endIndex = endIndex.substring(0, endIndex.indexOf("."));
				if (!startIndex.equals("") && !endIndex.equals("")) {
					componentValue = componentValue.substring(Integer.valueOf(startIndex) - 1, Integer.valueOf(endIndex));
				}
				String matchType = ExcelHandler.getCellValueX(inPutWorkBookx, module, excelRow, 10).trim();
				// for the validation if match type is blank
				// then it will use equals method to
				// compare.
				if (matchType.equalsIgnoreCase("contains") && captureLable.contains(componentValue)) {
					throw new CCSeleniumException("Encryption is not worked for  - " + logicalComponentID);
				} else if (!matchType.equalsIgnoreCase("contains") && captureLable.equals(componentValue)) {
					throw new CCSeleniumException("Encryption is not worked for - " + logicalComponentID);
				}
			}
			HandleSeleniumExecution.stepResult = true;
		
		}
		catch (Exception e) {
			HandleSeleniumExecution.stepResult = false;
			logger.error("Error in initiating component action", e);
			throw new CCSeleniumException(e.getMessage());
			// throw new CCSeleniumException("Error is - " + e.getMessage());

		}
		return HandleSeleniumExecution.stepResult;
	}  

	/**
	 * 
	 * 
	 * @param selenium
	 * @param element
	 * @param exrow
	 * @param module
	 * @param book
	 * @param componentType
	 * @param driver
	 * @throws Exception
	 */

	public static void waitforElement(Selenium selenium, String componentID, ExcelRow exrow, String module, XSSFWorkbook book,
			String componentType, WebDriver driver) throws Exception {
		// logger.debug("waitforElement() component id " + element);

		if (componentID != null && !componentID.equalsIgnoreCase("wait")) {
			try {
				int second;
				int frame = 1;
				int count = 1;
				WebDriver driverInstance = ((WebDriverBackedSelenium) selenium).getWrappedDriver();
				List<WebElement> frameset = null;
				try {
					frameset = driverInstance.findElements(By.tagName("iframe"));
				} catch (Exception e) {
					logger.warn("Error not expected here in waitForElement");
					// System.out.println("Error not expected");
				}
				for (second = 0; second <= 40; second++) {
					// System.out.println("Waiting for Element = "+element);
					if (second >= 20) {
						if (frameset != null && frameset.size() > 0 && frame <= frameset.size()) {
							driverInstance.switchTo().defaultContent();
							driverInstance.switchTo().frame(frame);
						} else {
							break;
						}
						frame++;
					}
					if (componentType != null && componentType.equalsIgnoreCase("menu") && selenium.isTextPresent(componentID)) {
						break;
					}
					if (selenium.isElementPresent(componentID)) {

						try {
							if (selenium.isVisible(componentID)) {
								break;
							}
						} catch (Exception ex) {
							logger.error("Selenium exception occurred during visiblity check. Error is - " + ex, ex);
						}
					}
					boolean breakOuter = false;
					
					Thread.sleep(1000);
					count++;
					if (breakOuter)
						break;
				}
			} catch (Exception e) {
				logger.error("waitForElement() Element id, " + componentID + ", not found. Either element in some iframe or Xpath is incorrect.", e);
				throw new ElementNotFoundException("Element id, " + componentID + ", not found. Error is - " + e);
			}
		}
	}

}
