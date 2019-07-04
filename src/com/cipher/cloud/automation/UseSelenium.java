package com.cipher.cloud.automation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cipher.cloud.automation.utils.ExcelHandler;
import com.cipher.cloud.automation.utils.PropertyFileReader;
import com.cipher.cloud.exceptions.InputFileReadException;
import com.cipher.cloud.exceptions.InputFilesNotFoundException;
import com.cipher.cloud.exceptions.MasterRepositoryFileNotFoundException;
import com.cipher.cloud.reporting.ReportGenerator;

public class UseSelenium {

	private static Logger logger = Logger.getLogger(UseSelenium.class);

	public static String testOutputLocation = null;
	public static String outputLocation = null;
	private static XSSFWorkbook inputWorkbook = null;
	public static XSSFWorkbook masterRepositoryWorkBook = null;

	public static boolean generateHTMLReport = false;
	public static long dynamicComponentHandlingWait = 5000l;
	public static String orgUsername = null;
	public static String orgPassword = null;

	/**
	 * 
	 * 
	 * 
	 * @param inputfilePath
	 * @param outputFilePath
	 * @param objectRepostitoryLocation
	 * @return
	 */
	public static String setConfiguration(String inputfilePath, String outputFilePath, String objectRepostitoryLocation) {

		File outputFile = new File(outputFilePath + "/" + new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss").format(new Date()));

		if (!outputFile.exists()) {
			outputFile.mkdir();
		}

		outputLocation = outputFile.getAbsolutePath();

		/*try {
			FileUtils.copyDirectoryToDirectory(new File("Resources"), new File(outputLocation));
		} catch (IOException e) {
			logger.error("Error in environment setup. " + " Error is - " + e);
			ReportGenerator.generateErrorHTMLForFailedConfiguaration(e);
			return "Error in environment setup. " + " Error is - " + e;
		}*/

		logger.debug("[Environment setup] Created Output Folder at - " + outputLocation);

		try {
			PropertyFileReader.setProperties("config.properties");
		} catch (Exception e) {
			logger.error("Error in environment setup. " + " Error is - ", e);
			ReportGenerator.generateErrorHTMLForFailedConfiguaration(e);
			return "Error in environment setup. " + " Error is - " + e;
		}

		/**
		 * Read object repository
		 */
		try {
			ObjectRepository.readObjectRepository(objectRepostitoryLocation);
		} catch (Exception e) {
			logger.error("Error in environment setup. " + " Error is - ", e);
			ReportGenerator.generateErrorHTMLForFailedConfiguaration(e);
			return "Error in environment setup. " + " Error is - " + e;
		}

		/**
		 * Read input files
		 */
		File inputFolder = new File(inputfilePath + "/");
		File[] inputFileList = inputFolder.listFiles();

		boolean inputFilesFound = false;
		for (File inputFile : inputFileList) {
			if (inputFile.isFile()) {
				if (inputFile.getName().startsWith("Test_Input")) {
					inputFilesFound = true;
					break;
				}
			}
		}
		String masterRepositoryFileName = "";
		boolean masterRepositoryFileFound = false;
		for (File inputFile : inputFileList) {
			if (inputFile.isFile()) {
				if (inputFile.getName().startsWith("MasterRepository")) {
					masterRepositoryFileFound = true;
					masterRepositoryFileName = inputFile.getName();
					try {
						masterRepositoryWorkBook = ExcelHandler.createReadWriteWorkBook(inputfilePath, masterRepositoryFileName);
					} catch(Exception e){
						logger.error("Error in Test setup for master repository file, " + masterRepositoryFileName + ". Error is - ", e);
					}
					break;
				}
			}
		}

		try {
			if (!inputFilesFound) {
				throw new InputFilesNotFoundException(inputfilePath);
			}
			logger.debug("Input files found at location - " + inputfilePath);
			
			if (!masterRepositoryFileFound) {
				throw new MasterRepositoryFileNotFoundException(inputfilePath);
			}
			logger.debug("Master Repository file found at location - " + inputfilePath);
			
		} catch (Exception e) {
			logger.error("Error in environment setup. " + " Error is - ", e);
			ReportGenerator.generateErrorHTMLForFailedConfiguaration(e);
			return "Error in environment setup. " + " Error is - " + e;
		}
		return "";
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param inputfilePath
	 */

	public static void runSelenium(String inputfilePath) {
		String currentInputFileName = "";

		File inputFolder = new File(inputfilePath + "/");
		File[] inputFileList = inputFolder.listFiles();

		/**
		 * Iterate through the input files and execute tests
		 */
		for (File inputFile : inputFileList) {
			if (inputFile.isFile()) {

				if (inputFile.getName().startsWith("Test_Input")) {
					currentInputFileName = inputFile.getName();
					logger.debug("[Test Batch start] for file : " + currentInputFileName);
					try {

						logger.debug("[Test Batch setup] Processing file - " + inputFile.getPath());

						File screenshotFolder = new File(outputLocation + "/" + new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss").format(new Date()) + "_" + currentInputFileName);

						if (!screenshotFolder.exists()) {
							screenshotFolder.mkdir();
						}

						testOutputLocation = screenshotFolder.getAbsolutePath();

						logger.debug("[Test Batch Setup] Creating Output Folder for " + currentInputFileName + " at - " + screenshotFolder.getAbsolutePath());

						try {
							inputWorkbook = ExcelHandler.createReadWriteWorkBook(inputfilePath, currentInputFileName);
						} catch (Exception e) {
							throw new InputFileReadException(inputFile.getAbsolutePath(), e);
						}
						/**
						 * Call to test method to execute
						 */
						new InitiateTest(inputWorkbook).preparePriorityListAndExecute();
						ReportGenerator.generateTestReport(currentInputFileName);
						ReportGenerator.generateSFDCTestReport(inputWorkbook);
					} catch (Exception e) {
						logger.error("Error in Test setup for input file, " + currentInputFileName + ". Error is - ", e);
						ReportGenerator.generateTestReport(currentInputFileName, e);
						ReportGenerator.generateSFDCTestReport(inputWorkbook);
					}

					if (inputWorkbook != null) {
						ExcelHandler.saveAndCloseWorkBook(inputWorkbook);
					}
					if(masterRepositoryWorkBook != null){
						ExcelHandler.saveAndCloseWorkBook(masterRepositoryWorkBook);
					}
					HandleSeleniumExecution handleSeleniumExecution = new HandleSeleniumExecution();
					handleSeleniumExecution.stopSelenium();
					logger.debug("[Test Batch End] for file - " + currentInputFileName);
				}
			}
		}
		logger.debug("Selenium Execution Ended");
	}

	// public static void main(String[] args) {
	// String inputfilePath = "C:/Seleniumx/Input Files of Selenium testing/"
	// + args[0];
	// String outputFilePath = "C:/Selenium/Input Files of Selenium testing/"
	// + args[0];
	// String objectRepostitoryLocation =
	// "C:/Selenium/Input Files of Selenium testing/"
	// + args[0] + "/ObjectRepository.xlsx";
	//
	// if (UseSelenium.setConfiguration(inputfilePath, outputFilePath,
	// objectRepostitoryLocation).equals(""))
	// UseSelenium.runSelenium(inputfilePath);
	// }

}