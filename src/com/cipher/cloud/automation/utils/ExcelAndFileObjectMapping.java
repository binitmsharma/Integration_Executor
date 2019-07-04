package com.cipher.cloud.automation.utils;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelAndFileObjectMapping {
	private XSSFWorkbook outPutWorkBookx;
	private File excelFileObject;
	private String filepath;
	private String tempFilePath = null;
	private FileInputStream excelFileStream;

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public XSSFWorkbook getOutPutWorkBookx() {
		return outPutWorkBookx;
	}

	public void setOutPutWorkBookx(XSSFWorkbook outPutWorkBookx) {
		this.outPutWorkBookx = outPutWorkBookx;
	}

	public File getExcelFileObject() {
		return excelFileObject;
	}

	public void setExcelFileObject(File excelFileObject) {
		this.excelFileObject = excelFileObject;
	}

	public FileInputStream getExcelFileStream() {
		return excelFileStream;
	}

	public void setExcelFileStream(FileInputStream excelFileStream) {
		this.excelFileStream = excelFileStream;
	}

	public String getTempFilePath() {
		return tempFilePath;
	}

	public void setTempFilePath(String tempFilePath) {
		this.tempFilePath = tempFilePath;
	}
}
