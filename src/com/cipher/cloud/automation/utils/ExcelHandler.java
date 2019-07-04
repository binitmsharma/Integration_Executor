package com.cipher.cloud.automation.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cipher.cloud.exceptions.CCSeleniumException;
import com.cipher.cloud.exceptions.CreateWorkbookFailedException;
import com.cipher.cloud.exceptions.ObjectRepositoryReadException;
import com.cipher.cloud.exceptions.SheetNotFoundException;

public class ExcelHandler {
	private static Logger logger = Logger.getLogger(ExcelHandler.class);

	private static HashMap<XSSFWorkbook, ExcelAndFileObjectMapping> excelToFileMapping = null;
	private static HashMap<HSSFWorkbook, ExcelAndFileObjectMappingHSSF> excelToFileMappingHSSF = null;
	/**
	 * 
	 * @param excelPathAndName
	 * @return
	 * @throws CreateWorkbookFailedException
	 */
	public static XSSFWorkbook createReadOnlyWorkBook(String excelPathAndName) throws CreateWorkbookFailedException {
		logger.debug("createWorkBook() Attempting to create read-only workbook");
		XSSFWorkbook outPutWorkBookx = null;
		try {
			if (excelToFileMapping == null) {
				excelToFileMapping = new HashMap<XSSFWorkbook, ExcelAndFileObjectMapping>();
			}
			File outputFileTmp = new File(excelPathAndName);
			FileInputStream fileOut = new FileInputStream(outputFileTmp);
			outPutWorkBookx = new XSSFWorkbook(excelPathAndName);
			ExcelAndFileObjectMapping mappingObject = new ExcelAndFileObjectMapping();
			mappingObject.setExcelFileObject(outputFileTmp);
			mappingObject.setFilepath(excelPathAndName);
			mappingObject.setExcelFileStream(fileOut);
			mappingObject.setOutPutWorkBookx(outPutWorkBookx);
			excelToFileMapping.put(outPutWorkBookx, mappingObject);
		} catch (Exception e) {
			logger.error("Exception in opening the file " + excelPathAndName + ". Error is - " + e);
			throw new CreateWorkbookFailedException(e);
		}
		logger.debug("createWorkBook() Read-only workbook created successfully");
		return outPutWorkBookx;
	}

	/**
	 * 
	 * @param excelPath
	 * @param excelFileName
	 * @return
	 * @throws CreateWorkbookFailedException
	 */
	public static XSSFWorkbook createReadWriteWorkBook(String excelPath, String excelFileName) throws CreateWorkbookFailedException {
		logger.debug("createWorkBook() Attempting to create read-write workbook");
		XSSFWorkbook outPutWorkBookx = null;
		try {
			if (excelToFileMapping == null) {
				excelToFileMapping = new HashMap<XSSFWorkbook, ExcelAndFileObjectMapping>();
			}
			File outputFileTmp = new File(excelPath + "\\" + excelFileName);
			FileInputStream fileOut = new FileInputStream(outputFileTmp);
			outPutWorkBookx = new XSSFWorkbook(fileOut);
			// CreateTempFile
			FileOutputStream saveExcel = new FileOutputStream(new File(excelPath + "\\" + "Temporary" + excelFileName));
			outPutWorkBookx.write(saveExcel);
			saveExcel.flush();
			saveExcel.close();
			fileOut.close();

			outputFileTmp = new File(excelPath + "\\" + "Temporary" + excelFileName);
			fileOut = new FileInputStream(outputFileTmp);
			outPutWorkBookx = new XSSFWorkbook(fileOut);
			ExcelAndFileObjectMapping mappingObject = new ExcelAndFileObjectMapping();
			mappingObject.setExcelFileObject(outputFileTmp);
			mappingObject.setFilepath(excelPath + "\\" + excelFileName);
			mappingObject.setExcelFileStream(fileOut);
			mappingObject.setTempFilePath(excelPath + "\\" + "Temporary" + excelFileName);
			mappingObject.setOutPutWorkBookx(outPutWorkBookx);
			excelToFileMapping.put(outPutWorkBookx, mappingObject);
		} catch (Exception e) {
			logger.error("Exception in opening the file " + excelPath + "\\" + excelFileName + ". Error is -" + e);
			throw new CreateWorkbookFailedException(e);
		}
		logger.debug("createWorkBook() Read-write workbook created successfully");
		return outPutWorkBookx;
	}

	/**
	 * 
	 * @param outPutWorkBookx
	 */
	public static void saveAndCloseWorkBook(XSSFWorkbook outPutWorkBookx) {
		logger.debug("saveAndCloseWorkBook() Attempting save and close workbook");
		try {
			if (excelToFileMapping != null) {
				FileInputStream fileStreamObj = excelToFileMapping.get(outPutWorkBookx).getExcelFileStream();
				String filepath = excelToFileMapping.get(outPutWorkBookx).getFilepath();
				fileStreamObj.close();
				FileOutputStream saveExcel = new FileOutputStream(new File(filepath));
				outPutWorkBookx.write(saveExcel);
				saveExcel.flush();
				saveExcel.close();
				if (excelToFileMapping.get(outPutWorkBookx).getTempFilePath() != null) {
					File tmpFile = new File(excelToFileMapping.get(outPutWorkBookx).getTempFilePath());
					if (tmpFile.delete()) {
						logger.debug("Temp File deleted - " + excelToFileMapping.get(outPutWorkBookx).getTempFilePath());
					} else {
						logger.warn("Temp File not deleted - " + excelToFileMapping.get(outPutWorkBookx).getTempFilePath() + ". Please delete it manually.");
					}
				}
				excelToFileMapping.remove(outPutWorkBookx);
			}
		} catch (Exception e) {
			logger.error("Error occurred in storing the file. Error is : " + e);
		}
		logger.debug("saveAndCloseWorkBook() Workbook saved and closed");
	}

	/**
	 * 
	 * @param outPutWorkBookx
	 * @param sheetId
	 * @param row
	 * @param col
	 * @return
	 * @throws Exception
	 */
			
	public static String getCellValueX(XSSFWorkbook outPutWorkBookx, int sheetId, int row, int col) throws Exception {
		Row excleRow;
		Cell cell;
		int cellTypeValue;
		XSSFSheet excelSheet = outPutWorkBookx.getSheetAt(sheetId);

		if (excelSheet == null) {
			throw new SheetNotFoundException(sheetId);
		}

		excleRow = excelSheet.getRow(row);
		cell = excleRow.getCell(col);
		if (cell != null) {
			cellTypeValue = cell.getCellType();
			if (cellTypeValue == Cell.CELL_TYPE_STRING) {
				return cell.getStringCellValue();
			} else if (cellTypeValue == Cell.CELL_TYPE_NUMERIC) {
				double cellValue = cell.getNumericCellValue();
				return String.valueOf(cellValue);
			} else if (cellTypeValue == Cell.CELL_TYPE_FORMULA) {
				XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(excelSheet.getWorkbook());
				CellValue c = evaluator.evaluate(cell);
				if (c.getCellType() == Cell.CELL_TYPE_STRING) {
					return c.getStringValue();
				} else if (c.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					double cellValue = c.getNumberValue();
					return String.valueOf(cellValue);
				}
			}
		}
		return "";
	}

	/**
	 * 
	 * @param inPutWorkBookx
	 * @param sheetName
	 * @param row
	 * @param col
	 * @return
	 * @throws Exception
	 */
	public static String getCellValueX(XSSFWorkbook inPutWorkBookx, String sheetName, int row, int col) throws Exception {
		Row excleRow;
		Cell cell;
		int cellTypeValue;

		XSSFSheet excelSheet = inPutWorkBookx.getSheet(sheetName);

		if (excelSheet == null) {
			throw new SheetNotFoundException(sheetName);
		}

		excleRow = excelSheet.getRow(row);
		cell = excleRow.getCell(col);
		if (cell != null) {
			cellTypeValue = cell.getCellType();
			if (cellTypeValue == Cell.CELL_TYPE_STRING) {
				return cell.getStringCellValue();
			} else if (cellTypeValue == Cell.CELL_TYPE_NUMERIC) {
				double cellValue = cell.getNumericCellValue();
				return String.valueOf(cellValue);
			} else if (cellTypeValue == Cell.CELL_TYPE_FORMULA) {
				XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(excelSheet.getWorkbook());
				CellValue c = evaluator.evaluate(cell);
				if (c.getCellType() == Cell.CELL_TYPE_STRING) {
					return c.getStringValue();
				} else if (c.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					double cellValue = c.getNumberValue();
					return String.valueOf(cellValue);
				}
			}
		}
		return "";
	}

	/**
	 * 
	 * @param outPutWorkBookx
	 * @param sheetName
	 * @param row
	 * @param col
	 * @param tmpContent
	 */
	public static void setCellValueX(XSSFWorkbook outPutWorkBookx, String sheetName, int row, int col, String tmpContent) {
		logger.debug("setCellValueX() Attempting to set cell value at Sheet : " + sheetName + " [Row : " + row + " | column : " + col + "]");
		outPutWorkBookx.getSheet(sheetName).getRow(row).getCell(col).setCellValue(tmpContent);
		logger.debug("setCellValueX() Cell value set in Sheet : " + sheetName + "[Row : " + row + " | column : " + col + "]");
	}
	
	/**
     * 
      * @param outPutWorkBookx
     * @param sheetId
     * @param row
     * @param col
     * @return
     * @throws Exception
     */
                   
     public static String getCellValueH(HSSFWorkbook outPutWorkBookx, int sheetId, int row, int col) throws Exception {
            Row excleRow;
            Cell cell;
            int cellTypeValue;
            HSSFSheet excelSheet = outPutWorkBookx.getSheetAt(sheetId);

            if (excelSheet == null) {
                   throw new SheetNotFoundException(sheetId);
            }

            excleRow = excelSheet.getRow(row);
            cell = excleRow.getCell(col);
            if (cell != null) {
                   cellTypeValue = cell.getCellType();
                   if (cellTypeValue == Cell.CELL_TYPE_STRING) {
                         return cell.getStringCellValue();
                   } else if (cellTypeValue == Cell.CELL_TYPE_NUMERIC) {
                         double cellValue = cell.getNumericCellValue();
                         return String.valueOf(cellValue);
                   } else if (cellTypeValue == Cell.CELL_TYPE_FORMULA) {
                         HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(excelSheet.getWorkbook());
                         CellValue c = evaluator.evaluate(cell);
                         if (c.getCellType() == Cell.CELL_TYPE_STRING) {
                                return c.getStringValue();
                         } else if (c.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                double cellValue = c.getNumberValue();
                                return String.valueOf(cellValue);
                         }
                   }
            }
            return "";
     }  
     
     /**
      * 
       * @param excelPathAndName
      * @return
      * @throws CreateWorkbookFailedException
      */
      public static HSSFWorkbook createReadOnlyHSSFWorkBook(String excelPathAndName) throws CreateWorkbookFailedException {
             logger.debug("createWorkBook() Attempting to create read-only workbook");
             HSSFWorkbook outPutWorkBookx = null;
             try {
                    if (excelToFileMappingHSSF == null) {
                          excelToFileMappingHSSF = new HashMap<HSSFWorkbook, ExcelAndFileObjectMappingHSSF>();
                    }
                    File outputFileTmp = new File(excelPathAndName);
                    FileInputStream fileOut = new FileInputStream(outputFileTmp);
                    outPutWorkBookx = new HSSFWorkbook(fileOut);
                    ExcelAndFileObjectMappingHSSF mappingObject = new ExcelAndFileObjectMappingHSSF();
                    mappingObject.setExcelFileObject(outputFileTmp);
                    mappingObject.setFilepath(excelPathAndName);
                    mappingObject.setExcelFileStream(fileOut);
                    mappingObject.setOutPutWorkBookx(outPutWorkBookx);
                    excelToFileMappingHSSF.put(outPutWorkBookx, mappingObject);

             } catch (Exception e) {
                    logger.error("Exception in opening the file " + excelPathAndName + ". Error is - " + e);
                    throw new CreateWorkbookFailedException(e);
             }
             logger.debug("createWorkBook() Read-only workbook created successfully");
             return outPutWorkBookx;
      }
	
	public static boolean readExcelValue(String fileLocation, int excelRow) throws Exception{
		HSSFWorkbook outputWorkbook = null;
		outputWorkbook = ExcelHandler.createReadOnlyHSSFWorkBook(fileLocation);

		String key = null;

		int excelSheetnumber = 0;

		String sheetName = null;

		try {
			int sheetCount = outputWorkbook.getNumberOfSheets();

			for (excelSheetnumber = 0; excelSheetnumber < sheetCount; excelSheetnumber++) {

				sheetName = outputWorkbook.getSheetAt(excelSheetnumber).getSheetName();
				if (sheetName.equalsIgnoreCase("EVC")) {

					key = ExcelHandler.getCellValueH(outputWorkbook, excelSheetnumber, 5, 13);
					System.out.println(key + "value of key");
					if (key != null && !key.equalsIgnoreCase("")) {
						System.out.println("not empty");
						return true;
					} else {
						logger.error("Unable to read excel " + fileLocation);
						logger.debug("Unable to read excel " + fileLocation);
						return false;
					}

				}
				logger.debug("[Environment setup] ReadExcelValue() completed successfully");
			}
		} catch (Exception e) {
			throw new ObjectRepositoryReadException(sheetName, sheetName, excelRow + 1, e);
		}
		return true;
	}
	
	
	
	public static boolean compareExcel(FileInputStream excellFile1,FileInputStream excellFile2)
	{ 
		try {
			int i=0;


			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook1 = new XSSFWorkbook(excellFile1);
			XSSFWorkbook workbook2 = new XSSFWorkbook(excellFile2);

			// Get  sheets from the workbook
			for(;i<4;i++)
			{
				XSSFSheet sheet1 = workbook1.getSheetAt(i);
				XSSFSheet sheet2 = workbook2.getSheetAt(i);
				System.out.println("Comparing sheet no "+i);
				// Compare sheets
				if(compareTwoSheets(sheet1, sheet2)) {
					System.out.println("\n\nThe two excel sheets are Equal");
				} else {
					System.out.println("\n\nThe two excel sheets are Not Equal");
					return false;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;

	}


// Compare Two Sheets
	public static boolean compareTwoSheets(XSSFSheet sheet1, XSSFSheet sheet2) {
		int firstRow1 = sheet1.getFirstRowNum();
		int lastRow1 = sheet1.getLastRowNum();
		boolean equalSheets = true;
		for(int i=firstRow1; i <= lastRow1; i++) {

			System.out.println("\n\nComparing Row "+i);

			XSSFRow row1 = sheet1.getRow(i);
			XSSFRow row2 = sheet2.getRow(i);
			if(!compareTwoRows(row1, row2)) {
				equalSheets = false;
				System.out.println("Row "+i+" - Not Equal");
				break;
			} else {
				System.out.println("Row "+i+" - Equal");

			}
		}
		return equalSheets;
	}

// Compare Two Rows
	public static boolean compareTwoRows(XSSFRow row1, XSSFRow row2) {
		if((row1 == null) && (row2 == null)) {
			return true;
		} else if((row1 == null) || (row2 == null)) {
			return false;
		}

		int firstCell1 = row1.getFirstCellNum();
		int lastCell1 = row1.getLastCellNum();
		boolean equalRows = true;

		// Compare all cells in a row
		for(int i=firstCell1; i <= lastCell1; i++) {
			XSSFCell cell1 = row1.getCell(i);
			XSSFCell cell2 = row2.getCell(i);
			if(!compareTwoCells(cell1, cell2)) {
				equalRows = false;
				System.err.println("       Cell "+i+" - NOt Equal");
				break;
			} else {
				System.out.println("       Cell "+i+" - Equal");

			}
		}
		return equalRows;
	}

// Compare Two Cells
	public static boolean compareTwoCells(XSSFCell cell1, XSSFCell cell2) {
		if((cell1 == null) && (cell2 == null)) {
			return true;
		} else if((cell1 == null) || (cell2 == null)) {
			return false;
		}

		boolean equalCells = false;
		int type1 = cell1.getCellType();
		int type2 = cell2.getCellType();
		if (type1 == type2) {
			if (cell1.getCellStyle().equals(cell2.getCellStyle())) {
				// Compare cells based on its type
				switch (cell1.getCellType()) {
				case HSSFCell.CELL_TYPE_FORMULA:
					if (cell1.getCellFormula().equals(cell2.getCellFormula())) {
						equalCells = true;
					}
					break;
				case HSSFCell.CELL_TYPE_NUMERIC:
					if (cell1.getNumericCellValue() == cell2
					.getNumericCellValue()) {
						equalCells = true;
					}
					break;
				case HSSFCell.CELL_TYPE_STRING:
					if (cell1.getStringCellValue().equals(cell2
							.getStringCellValue())) {
						equalCells = true;
					}
					break;
				case HSSFCell.CELL_TYPE_BLANK:
					if (cell2.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
						equalCells = true;
					}
					break;
				case HSSFCell.CELL_TYPE_BOOLEAN:
					if (cell1.getBooleanCellValue() == cell2
					.getBooleanCellValue()) {
						equalCells = true;
					}
					break;
				case HSSFCell.CELL_TYPE_ERROR:
					if (cell1.getErrorCellValue() == cell2.getErrorCellValue()) {
						equalCells = true;
					}
					break;
				default:
					if (cell1.getStringCellValue().equals(
							cell2.getStringCellValue())) {
						equalCells = true;
					}
					break;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
		return equalCells;
	}
	
	
	
	
	
	
	
}