package com.cipher.cloud.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cipher.cloud.automation.UseSelenium;
import com.cipher.cloud.automation.utils.ExcelHandler;
import com.gargoylesoftware.htmlunit.attachment.Attachment;

/**
 * @author binit.mohan
 * 
 * 
 */
public class ReportGenerator {
	
	private static Logger logger = Logger.getLogger(ReportGenerator.class);
	private static List<ExecutionResult> executionResults = new ArrayList<ExecutionResult>();
	static EnterpriseConnection connection;
	
	/**
	 * 
	 * @param inputWorkbook
	 */
	public static void generateSFDCTestReport(XSSFWorkbook inputWorkbook){
		  ConnectorConfig config = new ConnectorConfig();
		  if(!UseSelenium.orgUsername.isEmpty() || !UseSelenium.orgPassword.isEmpty()){ 
		  config.setUsername(UseSelenium.orgUsername);
		    config.setPassword(UseSelenium.orgPassword);
		    try {
		    	connection = Connector.newConnection(config);  
		    	createTestScenarioReports(inputWorkbook); 
		    } catch (ConnectionException e1) {
		    	logger.error("Unable to create connection with SalesForce Org. Either username, password or security token has changed.");
		    }
		  } else {
			  logger.error("Unable to create connection with SalesForce Org. Either username, password or security token is not supplied.");
		  }

	  }
	
	/**
	 * 
	 * @param inputWorkbook
	 */
	private static void createTestScenarioReports(XSSFWorkbook inputWorkbook) {

		logger.debug("Creating test cases result in SalesForce org");
		//String failedCaseId = null;
		try {
			if (executionResults.size() != 0) {
				for (ExecutionResult executionResult : executionResults) {
					List<ScenarioResult> scenarioResultList = executionResult.getScenarioResultList();
					int size = scenarioResultList.size();
					String []loc = new String[size];
					Test_Scenario__c[] testScenarios = new Test_Scenario__c[size];
					int i = 0;
					if(!scenarioResultList.isEmpty() || !(scenarioResultList.size() == 0)){
						for (ScenarioResult scenarioResult : scenarioResultList) {

							Test_Scenario__c testScenario = new Test_Scenario__c();
							testScenario.setDuration__c(Double.valueOf(scenarioResult.getExecutionTime()));
							testScenario.setError__c(scenarioResult.getError());
							String clientName = ExcelHandler.getCellValueX(inputWorkbook, executionResult.getModule(), 3, 2);
							String testEnvironment = ExcelHandler.getCellValueX(inputWorkbook, executionResult.getModule(), 3, 3);
							String testCycle = ExcelHandler.getCellValueX(inputWorkbook, executionResult.getModule(), 3, 4);
							String projectName = ExcelHandler.getCellValueX(inputWorkbook, executionResult.getModule(), 3, 5);
							testScenario.setProject__c(projectName);
							testScenario.setClient__c(clientName);
							testScenario.setType__c(testEnvironment);
							testScenario.setTest_Cycle__c(testCycle);
							testScenario.setScenario_Name__c(scenarioResult.getScenarioName());
							if(scenarioResult.isResult()){
								testScenario.setStatus__c("Pass");
							} else{
								testScenario.setStatus__c("Fail");


							}
							testScenario.setTestCase__c(scenarioResult.getScenarioName());
							testScenarios[i] = testScenario;
							loc[i] = UseSelenium.testOutputLocation.concat("/StepWiseReports/").concat(executionResult.getModule() + "/" + scenarioResult.getScenarioName() + ".html" );
							i++;

						}
						SaveResult []results = connection.create(testScenarios);
						int j = 0;
						for(SaveResult result : results){
							File f = new File(loc[j]);
								InputStream is = new FileInputStream(f);
								byte[] inbuff = new byte[(int)f.length()];        
								is.read(inbuff);

								Attachment attach = new Attachment();
								attach.setBody(inbuff);
								ScenarioResult scenarioResult = scenarioResultList.get(j);
								attach.setName(scenarioResult.getScenarioName() + ".html");
								attach.setIsPrivate(false);
								// attach to an object in SFDC 
								attach.setParentId(result.getId());
								if(!scenarioResult.isResult()) {
								SaveResult sr = connection.create(new com.sforce.soap.enterprise.sobject.SObject[] {attach})[0];
								is.close();
								if (sr.isSuccess()) {
									logger.debug("Successfully added attachment.");
								} else {
									logger.debug("Error adding attachment: " + sr.getErrors().length);
								} } j++;
							}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Unable to create test case or failed to create attachment");
		}    
	}

	/**
	 * 
	 * @param e
	 */
	public static void generateErrorHTMLForFailedConfiguaration(Exception e) {
		String indexHTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\">"
				+ "<title>Automation Result</title></head>"
				+ "<body style=\"width: 1024px; padding-left: 25px;\">"
				+ "<div style=\"border-bottom : solid black 1px;\"><table><tr><td>"
				+ "<img src=\"Resources/Logo.png\" style=\"height: 50%; width: 50%;\" />"
				+ "</td><td><h3>"
				+ "Automated Test Execution"
				+ "</h3></td></tr></table></div><br />";

		indexHTML = indexHTML
				+ "<div id=\"error\" style=\"padding-left: 10px;\">"
				+ "<table style=\"text-align: left; text-indent: 5px;\" border=\"1\">"
				+ "<tr><th><div style=\"min-width: 200px;\">"
				+ "Initialization for Automation Testing failed with the following error</div></th><td>"
				+ "<div style=\"min-width: 750px; \">" + e.getMessage()
				+ "</div></td></tr></table></div><br />";

		indexHTML = indexHTML + "</body></html>";

		generateHTMLFile(indexHTML, UseSelenium.outputLocation, "error");
	}

	/**
	 * 
	 * @param executionResult
	 * @return
	 */
	private static String buildModuleHTMLHeadAndHeading(ExecutionResult executionResult) {
		String color = "red";
		String result = "Fail";
		if (executionResult.isResult()) {
			color = "green";
			result = "Pass";
		}

		String headHTML = "<!DOCTYPE html><html>"
				+ "<head><meta charset=\"ISO-8859-1\">"
				+ "<title>"
				+ "Module Result - "
				+ executionResult.getModule()
				+ "</title>"
				+ "<script src=\"../Resources/js/jquery-1.11.0.min.js\"></script>"
				+ "<script src=\"../Resources/js/lightbox.min.js\"/></script>"
				+ "<link href=\"../Resources/css/lightbox.css\" rel=\"stylesheet\" />"
				+ "</head>"
				+ "<body style=\"max-width: 1024px; padding-left: 25px;\">";

		String bodyHeadingHTML = "<div id=\"header\" " + "style=\"background: "
				+ color + "; padding: 5px; max-width: 990px;\">"
				+ "<table><tr><td><div style=\"color: white; width: 940px;\">"
				+ executionResult.getModule() + "</div></td><td>"
				+ "<div style=\"border: white 1px solid; color: white\">"
				+ "<div style=\"padding-left: 5px; padding-right: 5px;\">"
				+ result + "</div></div></td></tr></table></div><br />";

		return headHTML + bodyHeadingHTML;
	}

	/**
	 * 
	 * @param executionResult
	 * @return
	 */
	private static String buildModuleHTMLError(ExecutionResult executionResult) {
		if (executionResult.isResult()) {
			return "";
		} else {
			return "<div id=\"error\" style=\"padding-left: 10px;\">"
					+ "<table style=\"text-align: left; text-indent: 5px;\">"
					+ "<tr><th><div style=\"min-width: 125px; border: black solid 1px;\">"
					+ "Failed Reason</div></th><td>"
					+ "<div style=\"min-width: 825px; border: black solid 1px;\">"
					+ executionResult.getError()
					+ "</div></td></tr></table></div><br />";
		}
	}

	/**
	 * 
	 * @param executionResult
	 * @return
	 */
	private static String buildModuleHTMLScenarioResults(ExecutionResult executionResult) {
		List<ScenarioResult> scenarioResults = executionResult.getScenarioResultList();

		if (scenarioResults.isEmpty() || scenarioResults.size() == 0) {
			System.out.println("what?");
			return "";
		} else {
			String scenarioResultHTML = "<div id=\"scenarioResults\" style=\"padding-left: 10px;\">"
					+ "<table style=\"text-align: left; text-indent: 5px;\">"
					+ "<tr><th style=\"vertical-align: middle; border: black solid 1px;\">"
					+ "<div style=\"min-width: 125px;\">Test Case Results</div></th><td>"
					+ "<div style=\"width: 825px; border: black 1px solid;\"><table>"
					+ "<tr style=\"text-align: center; background: black; color: white;\">"
					+ "<td><div style=\"width: 250px;\">Test Case Name</div></td>" ///--------
					+ "<td><div style=\"width: 75px;\">Result</div></td>"
					+ "<td><div style=\"width: 60px;\">Time (ms)</div></td>"
					+ "<td><div style=\"width: 420px;\">Failed Reason</div></td></tr>";

			int row = 0;
			for (ScenarioResult scenarioResult : scenarioResults) {
				System.out.println("abc");
				String color = "red";
				String result = "Fail";
				String background = "white";
				if (scenarioResult.isResult()) {
					color = "green";
					result = "Pass";
				}

				if ((row++) % 2 != 0) {
					background = "silver";
				}

				scenarioResultHTML = scenarioResultHTML
						+ "<tr style=\"font-size: small; background: "
						+ background
						+ "; text-indent: 5px;\">"
						+ "<td>"
						+ "<a href = \"StepWiseReports/" + executionResult.getModule()+"//"+scenarioResult.getScenarioName()
					    + ".html\" "+"style=\"text-decoration: none;\">"
						+ scenarioResult.getScenarioName()+"</a>" 
						+ "</td>"
						+ "<td style=\"background: "
						+ color
						+ "; color:white;\">"
						+ result
						+ "</td>"
						+ "<td style=\"text-align: right; padding-right: 5px;\">"
						+ scenarioResult.getExecutionTime() + "</td><td>"
						+ scenarioResult.getError() + "</td></tr>";

			}

			scenarioResultHTML = scenarioResultHTML + "</table></div></td></tr></table></div><br />";
			return scenarioResultHTML;
		}
	}

	/**
	 * 
	 * @param executionResult
	 * @return
	 */
	private static String buildModuleHTMLScreenshots(ExecutionResult executionResult) {
		String screenshotsHTML = "";
		List<ScenarioResult> scenarioResults = executionResult.getScenarioResultList();

		if (scenarioResults.isEmpty() || scenarioResults.size() == 0) {
			return "";
		} else {
			screenshotsHTML = "<div id=\"screenshots\" style=\"padding-left: 10px;\">"
					+ "<table style=\"text-align: left; text-indent: 5px;\"><tr>"
					+ "<th style=\"vertical-align: middle; border: black solid 1px;\">"
					+ "<div style=\"min-width: 125px;\">Screenshots</div></th><td><table>";
			for (ScenarioResult scenarioResult : scenarioResults) {
				screenshotsHTML = screenshotsHTML
						+ "<tr><td><div style=\"width: 825px; border: 1px black solid;\">"
						+ scenarioResult.getScenarioName() + "</div></td></tr>";
				List<String> screenshotList = scenarioResult.getScreenshotList();
				if (screenshotList.isEmpty() || screenshotList.size() == 0) {
					screenshotsHTML = screenshotsHTML + "<tr><td style=\"text-align: center;\">No screenshots taken</td></tr>";
				} else {
					screenshotsHTML = screenshotsHTML + "<tr><td><table><tr>";
					int count = 0;
					for (String screenshot : screenshotList) {
						screenshotsHTML = screenshotsHTML
								+ "<td><div style=\"height: 100px; width: 100px; border: silver 1px solid;\">"
								+ "<a href=\"" + screenshot
								+ "\" data-lightbox=\""
								+ scenarioResult.getScenarioName() + "\">"
								+ "<img style=\"width: 100%; height: 100%;\" "
								+ "alt=\"" + screenshot + "\" src=\""
								+ screenshot + "\" /></a></div></td>";

						if ((++count) % 7 == 0) {
							screenshotsHTML = screenshotsHTML + "</tr><tr>";
						}
					}
					screenshotsHTML = screenshotsHTML + "</tr></table></td></tr>";
				}
			}
		}
		return screenshotsHTML + "</table></td></tr></table></div></body></html>";
	}

	/**
	 * 
	 * @param executionResult
	 */
	public static void generateModuleReport(ExecutionResult executionResult) {
		executionResults.add(executionResult);
		if (UseSelenium.generateHTMLReport) {
			generateHTMLFile(buildModuleHTMLHeadAndHeading(executionResult)
					+ buildModuleHTMLError(executionResult)
					+ buildModuleHTMLScenarioResults(executionResult)
					+ buildModuleHTMLScreenshots(executionResult),
					UseSelenium.testOutputLocation, executionResult.getModule());
		}
	}

	/**
	 * 
	 * @param fileName
	 */
	public static void generateTestReport(String fileName) {
		generateTestReport(fileName, null);//call to generate module exe
	}

	/**
	 * 
	 * @param fileName
	 * @param e
	 */
	public static void generateTestReport(String fileName, Exception e) {
		
		if (UseSelenium.generateHTMLReport) {
			String indexHTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\">"
					+ "<title>Execution Result</title></head>"
					+ "<body style=\"width: 1024px; padding-left: 25px;\">"
					+ "<div style=\"border-bottom: solid black 1px;\"><table><tr><td>"
					+ "<img src=\"../Resources/Logo.png\" style=\"height: 50%; width: 50%;\" />"
					+ "</td><td><h3>"
					+ fileName
					+ "</h3></td></tr></table></div><br />";

			if (e != null) {
				indexHTML = indexHTML
						+ "<div id=\"error\" style=\"padding-left: 10px;\">"
						+ "<table style=\"text-align: left; text-indent: 5px;\" border= \"1\">"
						+ "<tr><th><div style=\"min-width: 125px; \">"
						+ "Initialization for the input file failed with the following error</div></th><td>"
						+ "<div style=\"min-width: 825px;\">" + e.getMessage()
						+ "</div></td></tr></table></div><br />";
			}

			indexHTML = indexHTML
					+ "<div><table border=\"1\"><tr style=\"text-align: center;\">"
					+ "<th><div style=\"width: 650px;\">Test Script Name</div></th>"
					+ "<th><div style=\"width: 75px;\">Execution Time (ms)</div></th>"
					+ "<th><div style=\"width: 75px;\">Status</div></th>"
					+ "<th><div style=\"width: 75px;\">Total Test Cases</div></th>"
					+ "<th><div style=\"width: 75px;\">Test Cases Passed</div></th></tr>";

			if (executionResults.size() != 0) {
				for (ExecutionResult executionResult : executionResults) {
					List<ScenarioResult> scenarioResultList = executionResult.getScenarioResultList();
					int totalScenario = scenarioResultList.size();
					int scenarioPassed = 0;
					for (ScenarioResult scenarioResult : scenarioResultList) {
						if (scenarioResult.isResult()) {
							scenarioPassed++;
						}
					}

					String color = "red";
					String status = "Failed";

					if (executionResult.isResult()) {
						color = "green";
						status = "Passed";
					}

					indexHTML = indexHTML + "<tr style=\"text-indent: 5px;\">"
							+ "<td><a href = \"" + executionResult.getModule()
							+ ".html\" " + "style=\"text-decoration: none;\">"
							+ executionResult.getModule() + "</a></td>"
							+ "<td>"
							+ executionResult.getTotalTimeForExecution()
							+ "</td>" + "<td><div style=\"background: " + color
							+ "; color: white\">" + status + "</div></td>"
							+ "<td>" + totalScenario + "</td><td>"
							+ scenarioPassed + "</td>";
					
					generateChartReportinHTML(executionResult.getModule(),totalScenario,scenarioPassed);
						
				}
			}

			//Start: Changes for displaying PieChart
			int imageFileCount=countImageInDirectory(UseSelenium.testOutputLocation);
			String []listImageInDirectory=new String[imageFileCount];
			listImageInDirectory=listImagePathInDirectory(UseSelenium.testOutputLocation);
			//End: Changes for displaying PieChart
			indexHTML = indexHTML + "</table></div>";
			
			//Start: Changes for displaying PieChart
			for(int i=0;i<imageFileCount;i++)
			{
				indexHTML=indexHTML+"<img style=\"width: 50%; height: 50%;\" "
						+ "\" src=\""
						+ listImageInDirectory[i] + "\" />";
				
			}
			//End: Changes for displaying PieChart
			
			/*indexHTML=indexHTML+"<img style=\"width: 50%; height: 50%;\" "
			+ "\" src=\""
			+ pieChartPath + "\" />";*/
			
			//End: Changes for displaying PieChart
			indexHTML = indexHTML + "</body></html>";
			generateHTMLFile(indexHTML, UseSelenium.testOutputLocation, "index");
		}
		try {
			generateExcelReport(UseSelenium.testOutputLocation);//call to generate seperate excel report for each scenario
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//executionResults = new ArrayList<ExecutionResult>();
	}

	

	
	private static String[] listImagePathInDirectory(String filePath) {
		String[] listImagePath= new String[10];
		int i=0;
		File f = new File(filePath);
		for (File file : f.listFiles()) {
			if (file.isFile() && (file.getName().endsWith(".jpg"))) {
			
			listImagePath[i++]=file.getAbsolutePath();
			}
		}
		return listImagePath;
	}

	private static int countImageInDirectory(String filePath) {
		 File f = new File(filePath);
		 
         int count = 0;
         for (File file : f.listFiles()) {
        	 if (file.isFile() && (file.getName().endsWith(".jpg"))) {
        		   count++; 
        	} 
         }
         
        return count;
	}

	
	private static String generateChartReportinHTML(String module, int totalScenario, int scenarioPassed) {
		DefaultPieDataset dataset = new DefaultPieDataset( );
		dataset.setValue("Pass", scenarioPassed);
 		dataset.setValue("Fail",(totalScenario-scenarioPassed));
 		
 		JFreeChart chart = ChartFactory.createPieChart(
 				module, // chart title
 		         dataset, // data
 		         true, // include legend
 		         true, false);
 		
 		
 		int width = 480; /* Width of the image */
	      int height = 320; /* Height of the image */ 
	      String pieChartLocation=UseSelenium.testOutputLocation+"\\"+"PieChart".concat(module)+(".jpg");
	      String pieChartPathWithReplaceSlash=pieChartLocation.replace("\\", "\\\\");
	      //logger.debug(">>>>>>>>>>>>>>>>>PieChart Path is : "+pieChartPathWithReplaceSlash);
	      File pieChart = new File(pieChartPathWithReplaceSlash);
	   
	      
	      try {
			ChartUtilities.saveChartAsJPEG( pieChart , chart , width , height );
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return pieChartPathWithReplaceSlash;
	}
	
	/**
	 * 
	 * @param file
	 * @param location
	 * @param fileName
	 */
	private static void generateHTMLFile(String file, String location, String fileName) {

		PrintWriter writer;
		try {
			if (!new File(location).exists()) {
				FileUtils.forceMkdir(new File(location));
			}
			writer = new PrintWriter(new File(location + "/" + fileName + ".html"));
			writer.println(file);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param location
	 * @throws IOException
	 */
	public static void generateExcelReport(String location) throws IOException {
		
		Workbook wb = new XSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		Sheet indexSheet = wb.createSheet("index");

		CellStyle mainHeadingCellStyle = wb.createCellStyle();
		mainHeadingCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		Font mainHeadingFont = wb.createFont();
		mainHeadingFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		mainHeadingFont.setFontHeightInPoints((short) 16);
		mainHeadingCellStyle.setFont(mainHeadingFont);

		Row mainHeadingRow = indexSheet.createRow(0);
		createCellAndSetValue(mainHeadingRow, (short) 0, "Automation Test Report", mainHeadingCellStyle);

		indexSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

		CellStyle headingCellStyle = wb.createCellStyle();
		headingCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		headingCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		headingCellStyle.setFillBackgroundColor(HSSFColor.BLACK.index);
		headingCellStyle.setFillForegroundColor(HSSFColor.BLACK.index);
		headingCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font headingFont = wb.createFont();
		headingFont.setColor(HSSFColor.WHITE.index);
		headingFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headingCellStyle.setFont(headingFont);
		headingCellStyle.setWrapText(true);

		Row headingRow = indexSheet.createRow((short) 2);
		createCellAndSetValue(headingRow, (short) 0, "Test Script Name", headingCellStyle);
		createCellAndSetValue(headingRow, (short) 1, "Status", headingCellStyle);
		createCellAndSetValue(headingRow, (short) 2, "Duration (ms)", headingCellStyle);
		createCellAndSetValue(headingRow, (short) 3, "Test Cases Executed", headingCellStyle);
		createCellAndSetValue(headingRow, (short) 4, "Test Cases Passed", headingCellStyle);

		Font dataFontWhite = wb.createFont();
		dataFontWhite.setColor(HSSFColor.WHITE.index);

		CellStyle dataCellStyleNormal = wb.createCellStyle();
		dataCellStyleNormal.setWrapText(true);

		CellStyle dataCellStyleGrey = wb.createCellStyle();
		dataCellStyleGrey.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
		dataCellStyleGrey.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		dataCellStyleGrey.setFillPattern(CellStyle.SOLID_FOREGROUND);
		dataCellStyleGrey.setWrapText(true);

		CellStyle dataCellStylePass = wb.createCellStyle();
		dataCellStylePass.setFillBackgroundColor(HSSFColor.GREEN.index);
		dataCellStylePass.setFillForegroundColor(HSSFColor.GREEN.index);
		dataCellStylePass.setFillPattern(CellStyle.SOLID_FOREGROUND);
		dataCellStylePass.setWrapText(true);
		dataCellStylePass.setFont(dataFontWhite);

		CellStyle dataCellStyleFail = wb.createCellStyle();
		dataCellStyleFail.setFillBackgroundColor(HSSFColor.RED.index);
		dataCellStyleFail.setFillForegroundColor(HSSFColor.RED.index);
		dataCellStyleFail.setFillPattern(CellStyle.SOLID_FOREGROUND);
		dataCellStyleFail.setWrapText(true);
		dataCellStyleFail.setFont(dataFontWhite);

		short rowCount = 3;
		for (ExecutionResult executionResult : executionResults) {
			generateExcelModuleReport(executionResult, wb); // to generate module wise report
		

			int scenariosExecuted = executionResult.getScenarioResultList().size();
			int scenariosPassed = 0;
			for (ScenarioResult scenarioResult : executionResult.getScenarioResultList()) {
				
				// to generate seperate excel for each scenario 
				//generateExcelStepReport( scenarioResult, wb);
				
				
				
				if (scenarioResult.isResult()) {
					scenariosPassed++;
				}
			}

			CellStyle cellStyle = dataCellStyleNormal;
			if (rowCount % 2 == 0) {
				cellStyle = dataCellStyleGrey;
			}

			Row row = indexSheet.createRow(rowCount++);
			createCellAndSetValue(row, (short) 0, executionResult.getModule(), cellStyle);

			Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_DOCUMENT);
			link.setAddress("'" + executionResult.getModule() + "'!A1");
			row.getCell(0).setHyperlink(link);

			if (executionResult.isResult()) {
				createCellAndSetValue(row, (short) 1, "Pass", dataCellStylePass);
			} else {
				createCellAndSetValue(row, (short) 1, "Fail", dataCellStyleFail);
			}

			createCellAndSetValue(row, (short) 2, "" + executionResult.getTotalTimeForExecution(), cellStyle);
			createCellAndSetValue(row, (short) 3, "" + scenariosExecuted, cellStyle);
			createCellAndSetValue(row, (short) 4, "" + scenariosPassed, cellStyle);
		}

		indexSheet.setColumnWidth(0, 19200);
		FileOutputStream fileOut = new FileOutputStream(location + "/Execution_Report.xlsx");

		wb.write(fileOut);
		fileOut.close();
	}

	/**
	 * 
	 * @param executionResult
	 * @param wb
	 * @throws IOException
	 */
	private static void generateExcelModuleReport(ExecutionResult executionResult, Workbook wb) throws IOException {
		
		Sheet sheet = wb.createSheet(executionResult.getModule());

		CellStyle mainHeadingCellStyle = wb.createCellStyle();
		mainHeadingCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		Font mainHeadingFont = wb.createFont();
		mainHeadingFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		mainHeadingFont.setFontHeightInPoints((short) 14);
		mainHeadingCellStyle.setFont(mainHeadingFont);

		Row mainHeadingRow = sheet.createRow(0);
		createCellAndSetValue(mainHeadingRow, (short) 0, executionResult.getModule(), mainHeadingCellStyle);

		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

		CellStyle headingCellStyle = wb.createCellStyle();
		headingCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		headingCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		headingCellStyle.setFillBackgroundColor(HSSFColor.BLACK.index);
		headingCellStyle.setFillForegroundColor(HSSFColor.BLACK.index);
		headingCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font headingFont = wb.createFont();
		headingFont.setColor(HSSFColor.WHITE.index);
		headingFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headingCellStyle.setFont(headingFont);
		headingCellStyle.setWrapText(true);

		Row headingRow = sheet.createRow((short) 2);
		//createCellAndSetValue(headingRow, (short) 0, "Test Case Name", headingCellStyle);
		createCellAndSetValue(headingRow, (short) 0, "Business Flow", headingCellStyle);
		createCellAndSetValue(headingRow, (short) 1, "Status", headingCellStyle);
		createCellAndSetValue(headingRow, (short) 2, "Duration (ms)", headingCellStyle);
		createCellAndSetValue(headingRow, (short) 3, "Error", headingCellStyle);

		Font dataFontWhite = wb.createFont();
		dataFontWhite.setColor(HSSFColor.WHITE.index);

		CellStyle dataCellStyleNormal = wb.createCellStyle();
		dataCellStyleNormal.setWrapText(true);

		CellStyle dataCellStyleGrey = wb.createCellStyle();
		dataCellStyleGrey.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
		dataCellStyleGrey.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		dataCellStyleGrey.setFillPattern(CellStyle.SOLID_FOREGROUND);
		dataCellStyleGrey.setWrapText(true);

		CellStyle dataCellStylePass = wb.createCellStyle();
		dataCellStylePass.setFillBackgroundColor(HSSFColor.GREEN.index);
		dataCellStylePass.setFillForegroundColor(HSSFColor.GREEN.index);
		dataCellStylePass.setFillPattern(CellStyle.SOLID_FOREGROUND);
		dataCellStylePass.setWrapText(true);
		dataCellStylePass.setFont(dataFontWhite);

		CellStyle dataCellStyleFail = wb.createCellStyle();
		dataCellStyleFail.setFillBackgroundColor(HSSFColor.RED.index);
		dataCellStyleFail.setFillForegroundColor(HSSFColor.RED.index);
		dataCellStyleFail.setFillPattern(CellStyle.SOLID_FOREGROUND);
		dataCellStyleFail.setWrapText(true);
		dataCellStyleFail.setFont(dataFontWhite);

		short rowCount = 3;
		for (ScenarioResult scenarioResult : executionResult
				.getScenarioResultList()) {
			
			

			CellStyle cellStyle = dataCellStyleNormal;
			if (rowCount % 2 == 0) {
				cellStyle = dataCellStyleGrey;
			}

			Row row = sheet.createRow(rowCount++);
			createCellAndSetValue(row, (short) 0, scenarioResult.getScenarioName(), cellStyle);
			if (scenarioResult.isResult()) {
				createCellAndSetValue(row, (short) 1, "Pass", dataCellStylePass);
			} else {
				createCellAndSetValue(row, (short) 1, "Fail", dataCellStyleFail);
			}

			createCellAndSetValue(row, (short) 2, "" + scenarioResult.getExecutionTime(), cellStyle);
			createCellAndSetValue(row, (short) 3, scenarioResult.getError(), cellStyle);
		}

		sheet.setColumnWidth(0, 15000);
		sheet.setColumnWidth(3, 25600);

	}
	
	
	
	/**
	 * 
	 * @param row
	 * @param index
	 * @param value
	 * @param cellStyle
	 */
	private static void createCellAndSetValue(Row row, short index, String value, CellStyle cellStyle) {
		Cell cell = row.createCell(index);
		cell.setCellValue(value);
		cell.setCellStyle(cellStyle);
	}
}
