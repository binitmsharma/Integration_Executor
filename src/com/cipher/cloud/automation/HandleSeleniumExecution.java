package com.cipher.cloud.automation;

import java.awt.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.util.Log;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cipher.cloud.automation.utils.ExcelHandler;
import com.cipher.cloud.automation.utils.ExcelRow;
import com.cipher.cloud.automation.utils.MiscellaneousUtil;
import com.cipher.cloud.exceptions.ElementNotFoundException;
import com.cipher.cloud.exceptions.CCSeleniumException;
import com.cipher.cloud.reporting.CreateStepWiseHtmlReport;
import com.cipher.cloud.reporting.ScenarioResult;
import com.google.common.io.Files;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.screentaker.ViewportPastingStrategy;

public class HandleSeleniumExecution extends BrowserConfiguration {
	 public static boolean stepResult;
	 public static int mstRepExcelRow;
	private static Logger logger = Logger.getLogger(HandleSeleniumExecution.class);

	public void execute() throws Exception {

	}

	/**
	 * @author binit.mohan
	 *
	 *         Removed scenario from parameter. This can be fetched from this
	 *         class. All the browser operation done in this method.
	 *
	 * @param excelRow
	 * @param module
	 * @param inPutWorkBook
	 * @param iteration
	 * @return
	 * @throws Exception
	 */
	public ScenarioResult executeFlow(int excelRow, String module, XSSFWorkbook inPutWorkBook, int iteration, String browser) throws Exception {
		ScenarioResult scenarioResult = new ScenarioResult();
		//StepResult stepResult = new StepResult();

		CreateStepWiseHtmlReport htmlStep= new 	CreateStepWiseHtmlReport();

		//String browser = null;
		String scenarioName;
		String componentType;
		String previousScenarioName = null;
		String componentID;
		String logicalComponentID;
		String componentValue;
		String takeValuesFromRepository;
		boolean masterRepositoryForRow = false;
		int mstRepExcelColumn = 3;
		DesiredCapabilities capabilities = null;
		String linkedRow = null;
		int linkedRowInt = 0;

		int success = -1;
		int iterations = 0;
		DateFormat dateFormat_ddMMyyyyHHmmss = new SimpleDateFormat("dd-MM-yyyy HHmmss");

		WebDriver driver = null;

		long startTimeOfScenario = (new Date()).getTime();

		scenarioName = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 0);

		logger.debug("[Scenario Run] executeFlow() execution started for Scenario : " + scenarioName);
		previousScenarioName = scenarioName + "-0" +iteration;
		//To create a step wise Html file
		htmlStep.createReport(previousScenarioName,module);
		try {
			//browser = openSelenium(scenarioName.split("-")[0], module, inPutWorkBook);

			if(selenium != null){
				driver = ((WebDriverBackedSelenium) selenium).getWrappedDriver();
			} else {
				String appiumExePath, deviceName, androidVersion, platform;
				System.setProperty("webdriver.chrome.driver", "C://Selenium//chromedriver.exe");
				//appiumExePath = ExcelHandler.getCellValueX(inputWorkbook, module, 1, 0);
				//deviceName = ExcelHandler.getCellValueX(inPutWorkBook, module, 1, 3);
				//androidVersion = ExcelHandler.getCellValueX(inputWorkbook, module, 2, 3);
				//platform = ExcelHandler.getCellValueX(inputWorkbook, module, 1, 0);
				capabilities = new DesiredCapabilities();
				File app= new File("C:\\Selenium\\com.salesforce.chatter.apk"); // Current file location of app in local media.
				//capabilities.setCapability("deviceName", deviceName);
				//capabilities.setCapability("deviceName", "080b7e41006991b3");
				//note 3
				capabilities.setCapability("deviceName", "34044a1651d22123");// Device unique ID
				capabilities.setCapability(CapabilityType.BROWSER_NAME, ""); //Name of mobile web browser to automate. Should be an empty string if automating an app instead.
				capabilities.setCapability(CapabilityType.VERSION, "5.0"); // Android version
				capabilities.setCapability("platformName", "Android"); // Name of platform
				capabilities.setCapability("newCommandTimeout","240");
				capabilities.setCapability("app",app.getAbsolutePath());
				capabilities.setCapability("app-package", "com.salesforce.chatter"); //Replace with your app's package
				capabilities.setCapability("app-activity", "salesfore.chatter.main"); //Replace with app's Activity
				driver = new RemoteWebDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities); // Appium running URL with port
			}

			do {

				componentType = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 4);
				logicalComponentID = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 3);
				componentID = ObjectRepository.getElementFromhashmap(logicalComponentID);
				takeValuesFromRepository = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 13);
				linkedRow = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 6);
				boolean masterRepositoryForColumn = false;

				//Handle condition
				if (componentType.contains("condition")) {
					componentValue = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 5);
						if (success == -1 && !componentType.contains("click")) {
							success = HTMLComponents.handleDynamicComponent(selenium, componentID, componentType, componentValue, success, driver, module);
						}

						if(success < 0){
							WebElement thisElement = HTMLComponents.findElement(driver, componentID);
							if(thisElement != null){
								HTMLComponents.handleDynamicComponentClick(selenium, inPutWorkBook, excelRow, driver, browser, componentID, componentType, module, thisElement);
								success = 1;
							}
							else {
								success = -1;
							}
						}
						if (success < 0) {
							// Get Component ID of Paging element
							logicalComponentID = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 15);
							componentID = ObjectRepository.getElementFromhashmap(logicalComponentID);
							WebElement thisElement = HTMLComponents.findElement(driver, componentID);
							if (thisElement == null) {
									logger.error("[Scenario Run] Error in Codition. Page Component ID not found." + componentID);
									throw new ElementNotFoundException("[Scenario Run] Error in Codition. Page Component ID not found." + componentID);
								}
							else {
								HTMLComponents.handleDynamicComponentClick(selenium, inPutWorkBook, excelRow, driver, browser, componentID, componentType, module, thisElement);
							}
						}

							componentType = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 4);
							logicalComponentID = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 3);
							componentID = ObjectRepository.getElementFromhashmap(logicalComponentID);
							WebElement thisElement = HTMLComponents.findElement(driver, componentID);
							if(thisElement != null){
								HTMLComponents.handleDynamicComponentClick(selenium, inPutWorkBook, excelRow, driver, browser, componentID, componentType, module, thisElement);
							}
							else {
								throw new ElementNotFoundException("[Scenario Run] Error in Codition. Page Component ID not found." + componentID);
							}



				}

				String validationRequired = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 7).trim();
				String encryptionCheckRequired = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 12).trim();
				boolean validate = validationRequired.equalsIgnoreCase("y");
				boolean encryptionCheck = encryptionCheckRequired.equalsIgnoreCase("y");
				// Check for next page to load in case of new page
				if (componentID != null && componentID.equalsIgnoreCase("wait")) {
					componentValue = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 5);
					// Mitul - if you want to wait for specific amount of time
					// update value in excel sheet
					if (componentValue != null && (Integer.parseInt(componentValue) != 0)) {
						System.out.println("No Zero Value :- " + componentValue);
						waitForPageLoad(driver, Integer.parseInt(componentValue));
					}
					// if wait value is given zero than you need to wait for
					// page load for default time.
					else {
						System.out.println("Zero Value :- " + componentValue);
						waitForPageLoad(driver, 70);
					}
					// Mitul
				}
				else {
					boolean menuClick = false;
					// waitforElement does not work with menu due to
					// mouse over event.
					componentType = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 4);
					if ((!componentType.equalsIgnoreCase("SwithToOpenWindow")) &&  (!componentType.equalsIgnoreCase("SwithToOpenTmpWindow")) &&
				         (!componentType.equalsIgnoreCase("CloseOpenWindow")) && (!componentType.equalsIgnoreCase("handleAlert")) &&
				         (!componentType.equalsIgnoreCase("MultiValidate")) && (!componentType.equalsIgnoreCase("ForceWait")) &&
				         (!componentType.equalsIgnoreCase("back")) && (!componentType.equalsIgnoreCase("forward")) &&
				         (!componentType.equalsIgnoreCase("Refresh")) && (!componentType.equalsIgnoreCase("Module")) &&
				         (!componentType.contains("condition"))) {
						try {
							ExcelRow exR = new ExcelRow(excelRow);
							if(!browser.equalsIgnoreCase("NativeApp")){
								HTMLComponents.waitforElement(selenium, componentID, exR, module, inPutWorkBook, componentType, driver);
							}
							if (excelRow != exR.getCount()) {
								excelRow = exR.getCount();
								logicalComponentID = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 3);
								componentID = ObjectRepository.getElementFromhashmap(logicalComponentID);
								iterations++;
							}
							if (iterations > 12) {
								iterations = 0;
								throw new ElementNotFoundException("Unable to find the component : " + componentID + " after " + iterations + " trials ");
							}
						} catch (Exception e) {
							logger.warn("Element is not present in the current page");
							try {
								driver.switchTo().defaultContent();
								driver.switchTo().frame(0);
							} catch (NoSuchFrameException ee) {
								logger.debug("No such frame found");
								//throw new ElementNotFoundException("Error in the waitforElement code. Error is - " + ee);
							}
						}
					}

					componentType = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 4);
					// if we have reached on the last menu option then click it
					if (!ExcelHandler.getCellValueX(inPutWorkBook, module,excelRow + 1, 4).equalsIgnoreCase("menu")
							&& !ExcelHandler.getCellValueX(inPutWorkBook, module,excelRow + 1, 4).trim().equalsIgnoreCase("")) {
						menuClick = true;
					}

					componentValue = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 5);


					if (componentType.equalsIgnoreCase("checkbox")) {
                    try{
						stepResult=  HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"checkbox", componentValue, selenium, inPutWorkBook, excelRow, module);

						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Checkbox should be clicked successfully", "Checkbox is clicked successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Checkbox should be clicked successfully", "Checkbox is  not clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());
							}

					}

					else if (componentType.equalsIgnoreCase("text")) {
						 try{
							 if(takeValuesFromRepository.equalsIgnoreCase("y")){
								 componentValue = ExcelHandler.getCellValueX(UseSelenium.masterRepositoryWorkBook, module, mstRepExcelRow, mstRepExcelColumn);
								 stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "text",componentValue, selenium, inPutWorkBook, excelRow, module);
								 masterRepositoryForRow = true;
								 masterRepositoryForColumn = true;
							 } else {
								 stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "text",componentValue, selenium, inPutWorkBook, excelRow, module);
							 }
							 //HTMLComponents.validationHandling(menuClick, browser,validate,encryptionCheck ,driver, componentID,"checkbox", componentValue, selenium, inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Text-' "+componentValue+" ' should be entered successfully", "Text-'" +componentValue+" ' is entered successfully", stepResult, previousScenarioName,module);}
						    catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Text-' "+componentValue+" ' should be entered successfully", "Text-'" +componentValue+" ' is not entered successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());
							}
					}


					else if (componentType.equalsIgnoreCase("label")) {
						try{ stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "label",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Label- '"+componentValue+" ' should be verified successfully", "Label- '"+componentValue+"' has been verified successfully", stepResult, previousScenarioName,module);}
						    catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Label- '"+componentValue+" ' should be verified successfully", "Label- '"+componentValue+"' has not been verified successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());   }
					}

					else if (componentType.equalsIgnoreCase("Refresh")) {

						driver.navigate().refresh();
					}

					else if (componentType.equalsIgnoreCase("Back")) {
						driver.navigate().back();
					}

					else if (componentType.equalsIgnoreCase("Forward")) {
						driver.navigate().forward();
					}

					else if (componentType.equalsIgnoreCase("NavigateToURL")) {
						driver.navigate().to(componentValue);

					}
					else if (componentType.equalsIgnoreCase("Clear")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Clear", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Textbox should be cleard successfully", "Textbox has been cleared", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Textbox should be cleard successfully", "Textbox has not been cleared", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if(componentType.equalsIgnoreCase("DoubleClick")){
                        try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"DoubleClick", componentValue,selenium, inPutWorkBook, excelRow,module);
                        if(stepResult)
                                      htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "File should be Uploaded successfully", "File has been uploaded successfully", stepResult, previousScenarioName,module);}
                        catch(Exception e){
                               htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "File should be Uploaded successfully", "File has not been uploaded successfully", stepResult, previousScenarioName,module);
                               throw new CCSeleniumException("Error is - " + e.getMessage());    }
                 }

					else if (componentType.equalsIgnoreCase("select-index")) {
						try{
							if(takeValuesFromRepository.equalsIgnoreCase("y")){
								componentValue = ExcelHandler.getCellValueX(UseSelenium.masterRepositoryWorkBook, module, mstRepExcelRow, mstRepExcelColumn);
								stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"select-index", componentValue,selenium, inPutWorkBook, excelRow,module);
								masterRepositoryForRow = true;
								masterRepositoryForColumn = true;
							} else {
								stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"select-index", componentValue,selenium, inPutWorkBook, excelRow,module);
							}

							if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "index should be selected successfully", "index has been selected successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "index should be selected successfully", "Index has not been verified successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }


					else if (componentType.equalsIgnoreCase("select-value")) {
						try{
							if(takeValuesFromRepository.equalsIgnoreCase("y")){
								componentValue = ExcelHandler.getCellValueX(UseSelenium.masterRepositoryWorkBook, module, mstRepExcelRow, mstRepExcelColumn);
								stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"select-value", componentValue,selenium, inPutWorkBook, excelRow,module);
								masterRepositoryForRow = true;
								masterRepositoryForColumn = true;
							} else {
								stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"select-value", componentValue,selenium, inPutWorkBook, excelRow,module);
							}
							if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "DropDown value- '"+componentValue+" ' should be selected successfully", "DropDown value- '"+componentValue+" ' has been selected successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "DropDown value- '"+componentValue+" ' should be selected successfully", "DropDown value- '"+componentValue+" ' has not been selected successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }


					else if (componentType.equalsIgnoreCase("radio")) {
						try{ stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "radio",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Radio Button should be selected successfully", "Radio Button has been selected successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Radio Button should be selected successfully", "Radio Button has not been selected successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());   }
						 }

					else if (componentType.equalsIgnoreCase("button")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"button", componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Button--'"+logicalComponentID+" ' should be clicked successfully", "Button"+logicalComponentID+" is clicked successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Button--'"+logicalComponentID+" ' should be clicked successfully", "Button"+logicalComponentID+" is not clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());   }
						 }

					else if (componentType.equalsIgnoreCase("href")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "href",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Link should be clicked successfully", "Link is clicked Successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Link should be clicked successfully", "link is not clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());   }
						 }

					else if (componentType.equalsIgnoreCase("Image")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "Image",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Image should be clicked successfully", "Image is clicked successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Image should be clicked successfully", "Image is not clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("menu")) {
						try{ stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "menu",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Menu--' "+logicalComponentID+" ' should be clicked successfully", "Menu has clicked successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Menu---' "+logicalComponentID+" ' should be clicked successfully", "Menu has not been clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());
						        }
						 }

					else if (componentType.equalsIgnoreCase("Frame")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "Frame",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Frame should be clicked successfully", "Frame is clicked successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Frame should be clicked successfully", "Frame has not been clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("Frameout")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Frameout", componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Frameout should be done successfully", "Frameout has been done successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Frameout should be done successfully", "Frameout has not been done successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("HandleAlert")) {
						try {stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"HandleAlert", componentValue, selenium,inPutWorkBook, excelRow, module);
						if(stepResult)
							htmlStep.writeToHtmlReport("Handle Alert", "Alert Should be handled successfully", "Alert is handled successfully", true, previousScenarioName,module);
						} catch (NoAlertPresentException e) {
							htmlStep.writeToHtmlReport("Handle Alert", "Alert Should be handled successfully", "Alert is not handled successfully", false, previousScenarioName,module);
							logger.warn("No alert Error occurred. Error is - " + e.getMessage());
							throw new CCSeleniumException("Error is - " + e.getMessage());
						}
					}

					else if(componentType.equalsIgnoreCase("UploadFile")){
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"UploadFile", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "File should be Uploaded successfully", "File has been uploaded successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "File should be Uploaded successfully", "File has not been uploaded successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if(componentType.equalsIgnoreCase("MouseHover")){
						try{ stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"MouseHover", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "MouseHover should happen successfully", "MouseHover has done successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "MouseHover should happen successfully", "MouseHover has not been done successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if(componentType.equalsIgnoreCase("ForceWait")){
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"ForceWait", componentValue,selenium, inPutWorkBook, excelRow,module);
						 //if(stepResult)
								//htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "System Should wait", "System is waiting for-"+componentValue+"Mili Seconds", stepResult, previousScenarioName,module);
						 }
						 catch(Exception e){
							//htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "System Should wait", "System has not waited for-"+componentValue+"Mili Seconds ", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if(componentType.equalsIgnoreCase("Command")){
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Command", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Command should run successfully", "Command executed successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Command should run successfully", "Command has not executed successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}
					else if (componentType.equalsIgnoreCase("isDisplayed")) {
						try{ boolean isDisplayed = HTMLComponents.findElement(driver, componentID).isDisplayed();
						if(isDisplayed)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should display to: "+logicalComponentID+" Page", "Element successfully navigate to: "+logicalComponentID+" Page",true , previousScenarioName,module);}
					 catch(Exception e){
						htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should navigate to: "+logicalComponentID+" Page", "Element did not navigate to: "+logicalComponentID+" Page", false, previousScenarioName,module);
						throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("isEnabled")) {
						try{ boolean isDisplayed = HTMLComponents.findElement(driver, componentID).isEnabled();
						if(isDisplayed)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should be enabled: "+logicalComponentID, "Element is enabled: "+logicalComponentID, true, previousScenarioName,module);}
					 catch(Exception e){
						htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should be enabled: "+logicalComponentID, "Element is not enabled: "+logicalComponentID, false, previousScenarioName,module);
						throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("isSelected")) {
						try{ boolean isDisplayed = HTMLComponents.findElement(driver, componentID).isSelected();
						if(isDisplayed)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should be selected: "+logicalComponentID, "Element is selected: "+logicalComponentID, true, previousScenarioName,module);}
					 catch(Exception e){
						htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should be selected: "+logicalComponentID, "Element is not selected: "+logicalComponentID, false, previousScenarioName,module);
						throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("SwithToOpenWindow")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"SwithToOpenWindow", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Window should be switched successfully", "Window has been swithched", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Window should be switched successfully", "Window has not been swithched", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("Copy")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Copy", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Ent order value should be copied successfully", "End order value has been copied successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Ent order value should be copied successfully", "End order value has not been copied successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("Paste")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Paste", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "ENT order value should be assigend successfully", "ENT order value has been assigned", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "ENT order value should be assigend successfully", "ENT order value has not been assigned", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("SwithToOpenTmpWindow")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"SwithToOpenTmpWindow", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Control should be switched to open Window successfully", "Window has been switched successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Control should be switched to open Window successfully", "Window has not been switched successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("SelectMultipelValues")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"SelectMultipelValues", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Multiple values should be selected successfully", "Multiple values has been selected successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Multiple values should be selected successfully", "Multiple values has not been selected successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("PDFFileCompare")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"PDFFileCompare", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "PDF files should be compared successfully", "PDF files has been compared successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "PDF files should be compared successfully", "PDF files has not been compared successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("ExcelFileCompare")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"ExcelFileCompare", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has been compared successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has not been compared successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("UnZip")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"UnZip", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has been compared successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has not been compared successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}
					else if (componentType.equalsIgnoreCase("ReadExcelValue")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"ReadExcelValue", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has been compared successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has not been compared successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("Module")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Module", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, componentValue + " module should be run successfully", "<a href = \""+UseSelenium.testOutputLocation+"\\StepWiseReports\\"+componentValue+"\""+"style=\"text-decoration: none;\">"+componentValue + " module has been run successfully"+"</a>", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, componentValue + " module should be run successfully", "<a href = \""+UseSelenium.testOutputLocation+"\\StepWiseReports\\"+componentValue+"\""+"style=\"text-decoration: none;\">"+componentValue + " module has not been run successfully"+"</a>", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equals("CloseOpenWindow")) {
						try{
							ManageWindows.closeOpenWindow(driver);
							htmlStep.writeToHtmlReport(module+"-CloseOpenWindow", "Window should be closed successfully", "Window has been closed", stepResult, previousScenarioName,module);
							}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Window should be closed successfully", "Window has not been closed", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					String screenshotRequired = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 11);
					if (screenshotRequired.equalsIgnoreCase("Y")) {
						takeScreenshots(driver, previousScenarioName, dateFormat_ddMMyyyyHHmmss, scenarioResult);
					}
					else if(screenshotRequired.equalsIgnoreCase("CompletePage")){
						takeCompletePageScreenshots(driver, previousScenarioName, dateFormat_ddMMyyyyHHmmss, scenarioResult);
					}

				}
				excelRow += 1;
				if(masterRepositoryForColumn){
					mstRepExcelColumn++;
				}
				scenarioName = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 0);
			} while (scenarioName == "");

			if(masterRepositoryForRow){
				mstRepExcelRow++;
			}
		} catch (UnhandledAlertException e) {
			logger.warn("UnhandledAlertException catched and avoided in scenario - " + scenarioName);
			takeScreenshots(driver, previousScenarioName, dateFormat_ddMMyyyyHHmmss, scenarioResult);
		} catch (Exception e) {
			if(linkedRow.isEmpty()){
				scenarioResult.setResult(false);
				if(e.getMessage().length()>=101){
					scenarioResult.setError(e.getMessage().substring(0, 100) + " [Sheet : " + module + " | Line number : " + (excelRow + 1) + "]");
				} else {
					scenarioResult.setError(e.getMessage() + " [Sheet : " + module + " | Line number : " + (excelRow + 1) + "]");
				}
				takeScreenshots(driver, previousScenarioName, dateFormat_ddMMyyyyHHmmss, scenarioResult);
				logger.error("executeFlow() failed for scenario : " + scenarioName + " [Sheet : " + module + " | Line Number :" + (excelRow + 1) + "]. Error is - " + e);
				do {
					excelRow += 1;
					scenarioName = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 0);
				} while (scenarioName == "");

				if(masterRepositoryForRow){
					mstRepExcelRow++;
				}
			}
			else {
				linkedRowInt = Integer.parseInt(linkedRow);
				excelRow = linkedRowInt - 1;
				System.out.println(excelRow);
				scenarioResult = MiscellaneousUtil.executeFlowForLinkedRow(previousScenarioName, htmlStep, scenarioName, scenarioResult, excelRow, module, inPutWorkBook, browser, selenium);
			}
		}

		long endTimeOfScenario = (new Date()).getTime();
		long timetaken = endTimeOfScenario - startTimeOfScenario;

		scenarioResult.setExecutionTime(timetaken);
		scenarioResult.setScenarioName(previousScenarioName);

		logger.debug("[Scenario End] executeFlow() execution comlpeted for Scenario :" + previousScenarioName + ". [Total time (in ms) :" + timetaken + "]");
		return scenarioResult;
	}


	/**
	 * To execute module(s) which is define in separate excel sheet and use inside any test case
	 *
	 * @author binit.mohan
	 * @param excelRow
	 * @param module
	 * @param inPutWorkBook
	 * @param iteration
	 * @param browser
	 * @return
	 * @throws Exception
	 */
	public ScenarioResult executeModuleFlow(int excelRow, String module, XSSFWorkbook inPutWorkBook, int iteration, String browser) throws Exception {
		ScenarioResult scenarioResult = new ScenarioResult();
		//StepResult stepResult = new StepResult();

		CreateStepWiseHtmlReport htmlStep= new 	CreateStepWiseHtmlReport();

		//String browser = null;
		String scenarioName;
		String componentType;
		String previousScenarioName = null;
		String componentID;
		String logicalComponentID;
		String componentValue;
		String takeValuesFromRepository;
		boolean masterRepositoryForRow = false;
		int mstRepExcelColumn = 3;
		DesiredCapabilities capabilities = null;
		String linkedRow = null;
		int linkedRowInt = 0;

		int success = -1;
		int iterations = 0;
		DateFormat dateFormat_ddMMyyyyHHmmss = new SimpleDateFormat("dd-MM-yyyy HHmmss");

		WebDriver driver = null;

		long startTimeOfScenario = (new Date()).getTime();

		scenarioName = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 0);

		logger.debug("[Scenario Run] executeFlow() execution started for Scenario : " + scenarioName);
		previousScenarioName = scenarioName + "-0" +iteration;
		//To create a step wise Html file
		htmlStep.createReport(previousScenarioName,module);
		try {
			//browser = openSelenium(scenarioName.split("-")[0], module, inPutWorkBook);

			if(selenium != null){
				driver = ((WebDriverBackedSelenium) selenium).getWrappedDriver();
			} else {
				String appiumExePath, deviceName, androidVersion, platform;
				System.setProperty("webdriver.chrome.driver", "C://Selenium//chromedriver.exe");
				//appiumExePath = ExcelHandler.getCellValueX(inputWorkbook, module, 1, 0);
				//deviceName = ExcelHandler.getCellValueX(inPutWorkBook, module, 1, 3);
				//androidVersion = ExcelHandler.getCellValueX(inputWorkbook, module, 2, 3);
				//platform = ExcelHandler.getCellValueX(inputWorkbook, module, 1, 0);
				capabilities = new DesiredCapabilities();
				File app= new File("C:\\Selenium\\com.salesforce.chatter.apk"); // Current file location of app in local media.
				//capabilities.setCapability("deviceName", deviceName);
				//capabilities.setCapability("deviceName", "080b7e41006991b3");
				//note 3
				capabilities.setCapability("deviceName", "34044a1651d22123");// Device unique ID
				capabilities.setCapability(CapabilityType.BROWSER_NAME, ""); //Name of mobile web browser to automate. Should be an empty string if automating an app instead.
				capabilities.setCapability(CapabilityType.VERSION, "5.0"); // Android version
				capabilities.setCapability("platformName", "Android"); // Name of platform
				capabilities.setCapability("newCommandTimeout","240");
				capabilities.setCapability("app",app.getAbsolutePath());
				capabilities.setCapability("app-package", "com.salesforce.chatter"); //Replace with your app's package
				capabilities.setCapability("app-activity", "salesfore.chatter.main"); //Replace with app's Activity
				driver = new RemoteWebDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities); // Appium running URL with port
			}

			do {

				componentType = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 4);
				logicalComponentID = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 3);
				componentID = ObjectRepository.getElementFromhashmap(logicalComponentID);
				takeValuesFromRepository = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 13);
				linkedRow = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 6);
				boolean masterRepositoryForColumn = false;

				//Handle condition
				if (componentType.contains("condition")) {
					componentValue = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 5);
						if (success == -1 && !componentType.contains("click")) {
							success = HTMLComponents.handleDynamicComponent(selenium, componentID, componentType, componentValue, success, driver, module);
						}
						if (success < 0) {
							// Get Component ID of Paging element
							logicalComponentID = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 15);
							componentID = ObjectRepository.getElementFromhashmap(logicalComponentID);
							WebElement thisElement = HTMLComponents.findElement(driver, componentID);
							if (thisElement == null) {
									logger.error("[Scenario Run] Error in Codition. Page Component ID not found." + componentID);
									throw new ElementNotFoundException("[Scenario Run] Error in Codition. Page Component ID not found." + componentID);
								}
							else {
								HTMLComponents.handleDynamicComponentClick(selenium, inPutWorkBook, excelRow, driver, browser, componentID, componentType, module, thisElement);
							}
						}

							componentType = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 4);
							logicalComponentID = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 3);
							componentID = ObjectRepository.getElementFromhashmap(logicalComponentID);
							WebElement thisElement = HTMLComponents.findElement(driver, componentID);
							if(thisElement != null){
								HTMLComponents.handleDynamicComponentClick(selenium, inPutWorkBook, excelRow, driver, browser, componentID, componentType, module, thisElement);
							}
							else {
								throw new ElementNotFoundException("[Scenario Run] Error in Codition. Page Component ID not found." + componentID);
							}



				}

				String validationRequired = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 7).trim();
				String encryptionCheckRequired = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 12).trim();
				boolean validate = validationRequired.equalsIgnoreCase("y");
				boolean encryptionCheck = encryptionCheckRequired.equalsIgnoreCase("y");
				// Check for next page to load in case of new page
				if (componentID != null && componentID.equalsIgnoreCase("wait")) {
					componentValue = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 5);
					// Mitul - if you want to wait for specific amount of time
					// update value in excel sheet
					if (componentValue != null && (Integer.parseInt(componentValue) != 0)) {
						System.out.println("No Zero Value :- " + componentValue);
						waitForPageLoad(driver, Integer.parseInt(componentValue));
					}
					// if wait value is given zero than you need to wait for
					// page load for default time.
					else {
						System.out.println("Zero Value :- " + componentValue);
						waitForPageLoad(driver, 70);
					}
					// Mitul
				}
				else {
					boolean menuClick = false;
					// waitforElement does not work with menu due to
					// mouse over event.
					componentType = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 4);
					if ((!componentType.equalsIgnoreCase("SwithToOpenWindow")) &&  (!componentType.equalsIgnoreCase("SwithToOpenTmpWindow")) &&
				         (!componentType.equalsIgnoreCase("CloseOpenWindow")) && (!componentType.equalsIgnoreCase("handleAlert")) &&
				         (!componentType.equalsIgnoreCase("MultiValidate")) && (!componentType.equalsIgnoreCase("ForceWait")) &&
				         (!componentType.equalsIgnoreCase("back")) && (!componentType.equalsIgnoreCase("forward")) &&
				         (!componentType.equalsIgnoreCase("Refresh")) && (!componentType.equalsIgnoreCase("Module"))) {
						try {
							ExcelRow exR = new ExcelRow(excelRow);
							if(!browser.equalsIgnoreCase("NativeApp")){
								HTMLComponents.waitforElement(selenium, componentID, exR, module, inPutWorkBook, componentType, driver);
							}
							if (excelRow != exR.getCount()) {
								excelRow = exR.getCount();
								logicalComponentID = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 3);
								componentID = ObjectRepository.getElementFromhashmap(logicalComponentID);
								iterations++;
							}
							if (iterations > 12) {
								iterations = 0;
								throw new ElementNotFoundException("Unable to find the component : " + componentID + " after " + iterations + " trials ");
							}
						} catch (Exception e) {
							logger.warn("Element is not present in the current page");
							try {
								driver.switchTo().defaultContent();
								driver.switchTo().frame(0);
							} catch (NoSuchFrameException ee) {
								logger.debug("No such frame found");
								//throw new ElementNotFoundException("Error in the waitforElement code or Element is not present in the current page. Error is - " + e);
							}
							
						}
					}

					componentType = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 4);
					// if we have reached on the last menu option then click it
					if (!ExcelHandler.getCellValueX(inPutWorkBook, module,excelRow + 1, 4).equalsIgnoreCase("menu")
							&& !ExcelHandler.getCellValueX(inPutWorkBook, module,excelRow + 1, 4).trim().equalsIgnoreCase("")) {
						menuClick = true;
					}

					componentValue = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 5);


					if (componentType.equalsIgnoreCase("checkbox")) {
                    try{
						stepResult=  HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"checkbox", componentValue, selenium, inPutWorkBook, excelRow, module);

						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Checkbox should be clicked successfully", "Checkbox is clicked successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Checkbox should be clicked successfully", "Checkbox is  not clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());
							}

					}

					else if (componentType.equalsIgnoreCase("text")) {
						 try{
							 if(takeValuesFromRepository.equalsIgnoreCase("y")){
								 componentValue = ExcelHandler.getCellValueX(UseSelenium.masterRepositoryWorkBook, module, mstRepExcelRow, mstRepExcelColumn);
								 stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "text",componentValue, selenium, inPutWorkBook, excelRow, module);
								 masterRepositoryForRow = true;
								 masterRepositoryForColumn = true;
							 } else {
								 stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "text",componentValue, selenium, inPutWorkBook, excelRow, module);
							 }
							 //HTMLComponents.validationHandling(menuClick, browser,validate,encryptionCheck ,driver, componentID,"checkbox", componentValue, selenium, inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Text-' "+componentValue+" ' should be entered successfully", "Text-'" +componentValue+" ' is entered successfully", stepResult, previousScenarioName,module);}
						    catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Text-' "+componentValue+" ' should be entered successfully", "Text-'" +componentValue+" ' is not entered successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());
							}
					}


					else if (componentType.equalsIgnoreCase("label")) {
						try{ stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "label",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Label- '"+componentValue+" ' should be verified successfully", "Label- '"+componentValue+"' has been verified successfully", stepResult, previousScenarioName,module);}
						    catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Label- '"+componentValue+" ' should be verified successfully", "Label- '"+componentValue+"' has not been verified successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());   }
					}

					else if (componentType.equalsIgnoreCase("Refresh")) {

						driver.navigate().refresh();
					}

					else if (componentType.equalsIgnoreCase("Back")) {
						driver.navigate().back();
					}

					else if (componentType.equalsIgnoreCase("Forward")) {
						driver.navigate().forward();
					}

					else if (componentType.equalsIgnoreCase("NavigateToURL")) {
						driver.navigate().to(componentValue);

					}
					else if (componentType.equalsIgnoreCase("Clear")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Clear", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Textbox should be cleard successfully", "Textbox has been cleared", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Textbox should be cleard successfully", "Textbox has not been cleared", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if(componentType.equalsIgnoreCase("DoubleClick")){
                        try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"DoubleClick", componentValue,selenium, inPutWorkBook, excelRow,module);
                        if(stepResult)
                                      htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "File should be Uploaded successfully", "File has been uploaded successfully", stepResult, previousScenarioName,module);}
                        catch(Exception e){
                               htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "File should be Uploaded successfully", "File has not been uploaded successfully", stepResult, previousScenarioName,module);
                               throw new CCSeleniumException("Error is - " + e.getMessage());    }
                 }

					else if (componentType.equalsIgnoreCase("select-index")) {
						try{
							if(takeValuesFromRepository.equalsIgnoreCase("y")){
								componentValue = ExcelHandler.getCellValueX(UseSelenium.masterRepositoryWorkBook, module, mstRepExcelRow, mstRepExcelColumn);
								stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"select-index", componentValue,selenium, inPutWorkBook, excelRow,module);
								masterRepositoryForRow = true;
								masterRepositoryForColumn = true;
							} else {
								stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"select-index", componentValue,selenium, inPutWorkBook, excelRow,module);
							}

							if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "index should be selected successfully", "index has been selected successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "index should be selected successfully", "Index has not been verified successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }


					else if (componentType.equalsIgnoreCase("select-value")) {
						try{
							if(takeValuesFromRepository.equalsIgnoreCase("y")){
								componentValue = ExcelHandler.getCellValueX(UseSelenium.masterRepositoryWorkBook, module, mstRepExcelRow, mstRepExcelColumn);
								stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"select-value", componentValue,selenium, inPutWorkBook, excelRow,module);
								masterRepositoryForRow = true;
								masterRepositoryForColumn = true;
							} else {
								stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"select-value", componentValue,selenium, inPutWorkBook, excelRow,module);
							}
							if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "DropDown value- '"+componentValue+" ' should be selected successfully", "DropDown value- '"+componentValue+" ' has been selected successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "DropDown value- '"+componentValue+" ' should be selected successfully", "DropDown value- '"+componentValue+" ' has not been selected successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }


					else if (componentType.equalsIgnoreCase("radio")) {
						try{ stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "radio",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Radio Button should be selected successfully", "Radio Button has been selected successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Radio Button should be selected successfully", "Radio Button has not been selected successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());   }
						 }

					else if (componentType.equalsIgnoreCase("button")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"button", componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Button--'"+logicalComponentID+" ' should be clicked successfully", "Button"+logicalComponentID+" is clicked successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Button--'"+logicalComponentID+" ' should be clicked successfully", "Button"+logicalComponentID+" is not clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());   }
						 }

					else if (componentType.equalsIgnoreCase("href")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "href",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Link should be clicked successfully", "Link is clicked Successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Link should be clicked successfully", "link is not clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());   }
						 }

					else if (componentType.equalsIgnoreCase("Image")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "Image",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Image should be clicked successfully", "Image is clicked successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Image should be clicked successfully", "Image is not clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("menu")) {
						try{ stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "menu",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Menu--' "+logicalComponentID+" ' should be clicked successfully", "Menu has clicked successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Menu---' "+logicalComponentID+" ' should be clicked successfully", "Menu has not been clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());
						        }
						 }

					else if (componentType.equalsIgnoreCase("Frame")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID, "Frame",componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Frame should be clicked successfully", "Frame is clicked successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Frame should be clicked successfully", "Frame has not been clicked successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("Frameout")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Frameout", componentValue, selenium,inPutWorkBook, excelRow, module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Frameout should be done successfully", "Frameout has been done successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Frameout should be done successfully", "Frameout has not been done successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("HandleAlert")) {
						try {stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"HandleAlert", componentValue, selenium,inPutWorkBook, excelRow, module);
						if(stepResult)
							htmlStep.writeToHtmlReport("Handle Alert", "Alert Should be handled successfully", "Alert is handled successfully", true, previousScenarioName,module);
						} catch (NoAlertPresentException e) {
							htmlStep.writeToHtmlReport("Handle Alert", "Alert Should be handled successfully", "Alert is not handled successfully", false, previousScenarioName,module);
							logger.warn("No alert Error occurred. Error is - " + e.getMessage());
							throw new CCSeleniumException("Error is - " + e.getMessage());
						}
					}

					else if(componentType.equalsIgnoreCase("UploadFile")){
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"UploadFile", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "File should be Uploaded successfully", "File has been uploaded successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "File should be Uploaded successfully", "File has not been uploaded successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if(componentType.equalsIgnoreCase("MouseHover")){
						try{ stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"MouseHover", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "MouseHover should happen successfully", "MouseHover has done successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "MouseHover should happen successfully", "MouseHover has not been done successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if(componentType.equalsIgnoreCase("ForceWait")){
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"ForceWait", componentValue,selenium, inPutWorkBook, excelRow,module);
						 //if(stepResult)
								//htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "System Should wait", "System is waiting for-"+componentValue+"Mili Seconds", stepResult, previousScenarioName,module);
						 }
						 catch(Exception e){
							//htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "System Should wait", "System has not waited for-"+componentValue+"Mili Seconds ", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if(componentType.equalsIgnoreCase("Command")){
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Command", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Command should run successfully", "Command executed successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Command should run successfully", "Command has not executed successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}
					else if (componentType.equalsIgnoreCase("isDisplayed")) {
						try{ boolean isDisplayed = HTMLComponents.findElement(driver, componentID).isDisplayed();
						if(isDisplayed)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should display to: "+logicalComponentID+" Page", "Element successfully navigate to: "+logicalComponentID+" Page",true , previousScenarioName,module);}
					 catch(Exception e){
						htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should navigate to: "+logicalComponentID+" Page", "Element did not navigate to: "+logicalComponentID+" Page", false, previousScenarioName,module);
						throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("isEnabled")) {
						try{ boolean isDisplayed = HTMLComponents.findElement(driver, componentID).isEnabled();
						if(isDisplayed)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should be enabled: "+logicalComponentID, "Element is enabled: "+logicalComponentID, true, previousScenarioName,module);}
					 catch(Exception e){
						htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should be enabled: "+logicalComponentID, "Element is not enabled: "+logicalComponentID, false, previousScenarioName,module);
						throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("isSelected")) {
						try{ boolean isDisplayed = HTMLComponents.findElement(driver, componentID).isSelected();
						if(isDisplayed)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should be selected: "+logicalComponentID, "Element is selected: "+logicalComponentID, true, previousScenarioName,module);}
					 catch(Exception e){
						htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Element Should be selected: "+logicalComponentID, "Element is not selected: "+logicalComponentID, false, previousScenarioName,module);
						throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("SwithToOpenWindow")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"SwithToOpenWindow", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Window should be switched successfully", "Window has been swithched", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Window should be switched successfully", "Window has not been swithched", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("Copy")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Copy", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Ent order value should be copied successfully", "End order value has been copied successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Ent order value should be copied successfully", "End order value has not been copied successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("Paste")) {
						try{stepResult= HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Paste", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "ENT order value should be assigend successfully", "ENT order value has been assigned", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "ENT order value should be assigend successfully", "ENT order value has not been assigned", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("SwithToOpenTmpWindow")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"SwithToOpenTmpWindow", componentValue,selenium, inPutWorkBook, excelRow,module);
						 if(stepResult)
								htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Control should be switched to open Window successfully", "Window has been switched successfully", stepResult, previousScenarioName,module);}
						 catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Control should be switched to open Window successfully", "Window has not been switched successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
						 }

					else if (componentType.equalsIgnoreCase("SelectMultipelValues")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"SelectMultipelValues", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Multiple values should be selected successfully", "Multiple values has been selected successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Multiple values should be selected successfully", "Multiple values has not been selected successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("PDFFileCompare")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"PDFFileCompare", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "PDF files should be compared successfully", "PDF files has been compared successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "PDF files should be compared successfully", "PDF files has not been compared successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("ExcelFileCompare")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"ExcelFileCompare", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has been compared successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has not been compared successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("UnZip")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"UnZip", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has been compared successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has not been compared successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}
					else if (componentType.equalsIgnoreCase("ReadExcelValue")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"ReadExcelValue", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has been compared successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has not been compared successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equalsIgnoreCase("Module")) {
						try{stepResult=HTMLComponents.generalHTMLElementHandling(menuClick, browser,validate,encryptionCheck, driver, componentID,"Module", componentValue,selenium, inPutWorkBook, excelRow,module);
						if(stepResult)
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has been compared successfully", stepResult, previousScenarioName,module);}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Excel files should be compared successfully", "Excel files has not been compared successfully", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					else if (componentType.equals("CloseOpenWindow")) {
						try{
							ManageWindows.closeOpenWindow(driver);
							htmlStep.writeToHtmlReport(module+"-CloseOpenWindow", "Window should be closed successfully", "Window has been closed", stepResult, previousScenarioName,module);
							}
						catch(Exception e){
							htmlStep.writeToHtmlReport(module+"-"+logicalComponentID, "Window should be closed successfully", "Window has not been closed", stepResult, previousScenarioName,module);
							throw new CCSeleniumException("Error is - " + e.getMessage());    }
					}

					boolean screenshotRequired = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 11).equalsIgnoreCase("Y");
					if (screenshotRequired) {
						takeScreenshots(driver, previousScenarioName, dateFormat_ddMMyyyyHHmmss, scenarioResult);
					}

				}
				excelRow += 1;
				if(masterRepositoryForColumn){
					mstRepExcelColumn++;
				}
				scenarioName = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 0);
			} while (scenarioName == "");

			if(masterRepositoryForRow){
				mstRepExcelRow++;
			}
		} catch (UnhandledAlertException e) {
			logger.warn("UnhandledAlertException catched and avoided in scenario - " + scenarioName);
			takeScreenshots(driver, previousScenarioName, dateFormat_ddMMyyyyHHmmss, scenarioResult);
		} catch (Exception e) {
			if(linkedRow.isEmpty()){
				scenarioResult.setResult(false);
				if(e.getMessage().length()>=101){
					scenarioResult.setError(e.getMessage().substring(0, 100) + " [Sheet : " + module + " | Line number : " + (excelRow + 1) + "]");
				} else {
					scenarioResult.setError(e.getMessage() + " [Sheet : " + module + " | Line number : " + (excelRow + 1) + "]");
				}
				takeScreenshots(driver, previousScenarioName, dateFormat_ddMMyyyyHHmmss, scenarioResult);
				logger.error("executeFlow() failed for scenario : " + scenarioName + " [Sheet : " + module + " | Line Number :" + (excelRow + 1) + "]. Error is - " + e);
				do {
					excelRow += 1;
					scenarioName = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 0);
				} while (scenarioName == "");

				if(masterRepositoryForRow){
					mstRepExcelRow++;
				}
			}
			else {
				linkedRowInt = Integer.parseInt(linkedRow);
				excelRow = linkedRowInt - 1;
				System.out.println(excelRow);
				scenarioResult = MiscellaneousUtil.executeFlowForLinkedRow(previousScenarioName, htmlStep, scenarioName, scenarioResult, excelRow, module, inPutWorkBook, browser, selenium);
			}
		}

		long endTimeOfScenario = (new Date()).getTime();
		long timetaken = endTimeOfScenario - startTimeOfScenario;

		scenarioResult.setExecutionTime(timetaken);
		scenarioResult.setScenarioName(previousScenarioName);

		logger.debug("[Scenario End] executeFlow() execution comlpeted for Scenario :" + previousScenarioName + ". [Total time (in ms) :" + timetaken + "]");
		return scenarioResult;
	}

	/**
	 * If you provide wait Than Wait for the page to load
	 *
	 * @param driver
	 * @param totalWaitForSeconds
	 */
	private void waitForPageLoad(WebDriver driver, int totalWaitForSeconds) {
		ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};
		Wait<WebDriver> wait = new WebDriverWait(driver, totalWaitForSeconds);
		try {
			long initMilliSec = System.currentTimeMillis();
			wait.until(expectation);
			logger.info("" + (System.currentTimeMillis() - initMilliSec));
		} catch (Throwable error) {
			logger.error("Timeout waiting for Page Load Request to complete.");
		}
	}

	/**
	 * This method takes screenshots of current view
	 *
	 * @param driver
	 * @param previousScenarioName
	 * @param dateFormat_ddMMyyyyHHmmss
	 * @param scenarioResult
	 * @throws IOException
	 */
	private void takeScreenshots(WebDriver driver, String previousScenarioName, DateFormat dateFormat_ddMMyyyyHHmmss, ScenarioResult scenarioResult) throws IOException {
		try{
			Date d = new Date();
			screenshotNumber++;
			File f = new File(UseSelenium.testOutputLocation + "/"+ previousScenarioName + "_" + dateFormat_ddMMyyyyHHmmss.format(d) + "_" + screenshotNumber + ".png");
			File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			Files.copy(scrFile, f);
			scenarioResult.addScreenshot(f.getName());
		} catch(Exception e){
			Log.debug("Error occured while taking screenshot");
			e.printStackTrace();
		}
	}

	/**
	 * This will take screenshots of complete page
	 *
	 * @param driver
	 * @param previousScenarioName
	 * @param dateFormat_ddMMyyyyHHmmss
	 * @param scenarioResult
	 * @throws IOException
	 */

	private void takeCompletePageScreenshots(WebDriver driver, String previousScenarioName, DateFormat dateFormat_ddMMyyyyHHmmss, ScenarioResult scenarioResult) throws IOException {
		try{
			Date d = new Date();
			screenshotNumber++;
			File f = new File(UseSelenium.testOutputLocation + "/"+ previousScenarioName + "_" + dateFormat_ddMMyyyyHHmmss.format(d) + "_" + screenshotNumber + ".png");
			final Screenshot screenshot = new AShot().shootingStrategy(new ViewportPastingStrategy(500)).takeScreenshot(driver);
			final BufferedImage image = screenshot.getImage();
			ImageIO.write(image, "PNG", f);
			scenarioResult.addScreenshot(f.getName());
		}
		catch(Exception ex){
			logger.debug("Failed to get the complete image.");
			takeScreenshots(driver, previousScenarioName, dateFormat_ddMMyyyyHHmmss, scenarioResult);
		}
	}
}
