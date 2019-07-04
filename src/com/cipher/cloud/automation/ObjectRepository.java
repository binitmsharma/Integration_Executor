package com.cipher.cloud.automation;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cipher.cloud.automation.utils.ExcelHandler;
import com.cipher.cloud.exceptions.LogicalIdNotFoundException;
import com.cipher.cloud.exceptions.ObjectRepositoryNotInitializedException;
import com.cipher.cloud.exceptions.ObjectRepositoryReadException;

public class ObjectRepository {
	private static Logger logger = Logger.getLogger(ObjectRepository.class);

	private static HashMap<String, String> objectRepository = new HashMap<String, String>();

	/**
	 * 
	 * @param objectRepositoryLocation
	 * @throws Exception
	 */
	public static void readObjectRepository(String objectRepositoryLocation)
			throws Exception {
		logger.debug("[Environment setup] readObjectRepository() method started " + "[File : " + objectRepositoryLocation + "]");

		XSSFWorkbook outputWorkbook = null;

		try {
			outputWorkbook = ExcelHandler.createReadOnlyWorkBook(objectRepositoryLocation);
		} catch (Exception e) {
			throw new ObjectRepositoryReadException(objectRepositoryLocation, e);
		}

		String key = null;
		String ComponentId = null;
		int excelSheetnumber = 0;
		int excelRow = 1;
		String sheetName = null;

		try {
			int sheetCount = outputWorkbook.getNumberOfSheets();

			for (excelSheetnumber = 0; excelSheetnumber < sheetCount - 1; excelSheetnumber++) {

				sheetName = outputWorkbook.getSheetAt(excelSheetnumber).getSheetName();

				for (excelRow = 1;; excelRow++) {
					key = ExcelHandler.getCellValueX(outputWorkbook, excelSheetnumber, excelRow, 4);
					if ("".equals(key)) {
						continue;
					}
					if (key == null || key.equalsIgnoreCase("end")) {
						break;
					}
					
					ComponentId = ExcelHandler.getCellValueX(outputWorkbook, excelSheetnumber, excelRow, 5).trim();

					objectRepository.put(key, ComponentId);
				}

				logger.debug("[Environment setup] Loaded object repository [sheet = " + sheetName + " | Total rows processed = " + (excelRow - 1) + "]");
			}
			logger.debug("[Environment setup] readObjectRepository() completed successfully");
		} catch (Exception e) {
			throw new ObjectRepositoryReadException(objectRepositoryLocation, sheetName, excelRow + 1, e);
		}
	}

	/**
	 * 
	 * @param LogicalId
	 * @return
	 * @throws Exception
	 */
	public static String getElementFromhashmap(String LogicalId)
			throws Exception {
		String componentID;
		if (objectRepository == null) {
			logger.error("Object repository not initialized properly. " + "Exception in getting the physical component ID");
			throw new ObjectRepositoryNotInitializedException();
		}
		componentID = objectRepository.get(LogicalId);
		if (componentID == null) {
			logger.error("Logical ID, " + LogicalId + ", not present in Object repositiry");
			throw new LogicalIdNotFoundException(LogicalId);
		}
		return componentID;
	}
}
