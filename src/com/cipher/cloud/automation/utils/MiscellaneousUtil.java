package com.cipher.cloud.automation.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.cipher.cloud.automation.HTMLComponents;
import com.cipher.cloud.automation.ManageWindows;
import com.cipher.cloud.automation.ObjectRepository;
import com.cipher.cloud.automation.UseSelenium;
import com.cipher.cloud.exceptions.CCSeleniumException;
import com.cipher.cloud.exceptions.ElementNotFoundException;
import com.cipher.cloud.reporting.CreateStepWiseHtmlReport;
import com.cipher.cloud.reporting.ScenarioResult;
import com.taguru.utility.PDFUtil;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

public class MiscellaneousUtil {
	private static Logger logger = Logger.getLogger(MiscellaneousUtil.class);
	
	private static final int BUFFER_SIZE = 4096;
	
	
	
	/**
	 * Compare PDF files
	 * @param file1  Downloaded file
	 * @param file2  Base file
	 * @return boolean
	 */
	
	public static boolean  comparePDFFiles(String file1, String file2){
		try{
			
			int file1_Count;
			int file2_Count;
			PDFUtil pdfUtil = new PDFUtil();
			file1_Count =  pdfUtil.getPageCount(file1); //returns the page count of file 
			file2_Count = pdfUtil.getPageCount(file2);
			if(file1_Count != file2_Count){
				System.out.println("Count Not equal");
				return false;
			}
			//returns the pdf content - all pages
			//pdfUtil.getText("c:/sample.pdf");
			//To store PDF pages as images
			//set the path where we need to store the images
			// pdfUtil.setImageDestinationPath("C:/Selenium");
			//pdfUtil.savePdfAsImage(file1);
			// System.out.println("File 1 stored");
			// pdfUtil.setImageDestinationPath("C:/Selenium");
			//pdfUtil.savePdfAsImage(file2);
			 //System.out.println("File 2 stored");
			 // compares the pdf documents & returns a boolean
			 // true if both files have same content. false otherwise.
			 logger.debug("Comparing PDF files"); 
			 pdfUtil.setImageDestinationPath("C:/Selenium/PDFResults");
			 pdfUtil.highlightPdfDifference(true);
			 boolean res = pdfUtil.comparePdfFilesBinaryMode(file1, file2);
			 if(!res){
				 return false;
			 }
			}catch(Exception e){
			e.printStackTrace();
		}
		return true;	
	}
	
	/**
	 * Get the latest file from the given folder
	 * @param String dir
	 * @return
	 */
	
	public static String lastFileModified(String dir) {
	    File fl = new File(dir);
	    File[] files = fl.listFiles(new FileFilter() {          
	        public boolean accept(File file) {
	            return file.isFile();
	        }
	    });
	    long lastMod = Long.MIN_VALUE;
	    File choice = null;
	    for (File file : files) {
	        if (file.lastModified() > lastMod) {
	            choice = file;
	            lastMod = file.lastModified();
	        }
	    }
	    return choice.getAbsolutePath();
	}
	
	
	/*public static String getCompletePath(String dir) {
		File files = new File(dir);
		File [] ls = files.listFiles();
		for(File file : ls){
			if(!file.isDirectory()){
				if(file.getName() == "Ethernet PRI - Fiber Sales Order Form.PDF")
					return file.getAbsolutePath();
			}
		}
		return "done";
	}*/
	
	/**
	 * Unzip Output.zip folder
	 * 
	 * @param filelocation
	 * @throws IOException
	 */
	public static void unZip(String filelocation) throws IOException{
		File destDir = new File(filelocation);
		if (!destDir.exists()) {
			destDir.mkdir();
		}

		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(filelocation+"\\Output.zip"));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			// String filePath = componentValue + File.separator
			// + entry.getName();
			String filePath = filelocation + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the
				// directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}
	
	/**
     * Extracts files form the Zip file
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }  
    
    
    public static ScenarioResult executeFlowForLinkedRow(String previousScenarioName, CreateStepWiseHtmlReport htmlStep,String scenarioName, ScenarioResult scenarioResult, int excelRow, String module, XSSFWorkbook inPutWorkBook, String browser, Selenium selenium) throws Exception {
    	
		String componentType;
		String componentID;
		String logicalComponentID;
		String componentValue;
		String takeValuesFromRepository;
		boolean masterRepositoryForRow = false;
		int mstRepExcelColumn = 3;

		int success = -1;

		WebDriver driver = null;
		//scenarioName = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 0);

		//To create a step wise Html file
		/*htmlStep.createReport(previousScenarioName,module);*/
		int mstRepExcelRow = 0;
		try {
			//browser = openSelenium(scenarioName.split("-")[0], module, inPutWorkBook);

			if(selenium != null){
				driver = ((WebDriverBackedSelenium) selenium).getWrappedDriver();
			}
			do {

				componentType = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 4);
				logicalComponentID = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 3);
				componentID = ObjectRepository.getElementFromhashmap(logicalComponentID);
				takeValuesFromRepository = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 13);
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
						//waitForPageLoad(driver, Integer.parseInt(componentValue));
					}
					// if wait value is given zero than you need to wait for
					// page load for default time.
					else {
						System.out.println("Zero Value :- " + componentValue);
						//waitForPageLoad(driver, 70);
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


					boolean stepResult = false;
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
			
			
		}
		catch (UnhandledAlertException e) {
			logger.warn("UnhandledAlertException catched and avoided in scenario - " + scenarioName);
			
		} catch (Exception e) {
			
				scenarioResult.setResult(false);
				if(e.getMessage().length()>=101){
					scenarioResult.setError(e.getMessage().substring(0, 100) + " [Sheet : " + module + " | Line number : " + (excelRow + 1) + "]");
				} else {
					scenarioResult.setError(e.getMessage() + " [Sheet : " + module + " | Line number : " + (excelRow + 1) + "]");
				}
				
				logger.error("executeFlow() failed for scenario : " + scenarioName + " [Sheet : " + module + " | Line Number :" + (excelRow + 1) + "]. Error is - " + e);
				do {
					excelRow += 1;
					scenarioName = ExcelHandler.getCellValueX(inPutWorkBook, module, excelRow, 0);
				} while (scenarioName == "");

				if(masterRepositoryForRow){
					mstRepExcelRow++;
					}
			 
		}
		return scenarioResult;
	}


}
