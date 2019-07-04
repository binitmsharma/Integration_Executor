package com.cipher.cloud.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cipher.cloud.automation.utils.ExcelHandler;
import com.cipher.cloud.exceptions.InputFileReadException;

public class PriorityExecutor {
	private static Logger logger = Logger.getLogger(PriorityExecutor.class);

	private Integer priority;
	private String module;
	private String comments;
	private long startTime;
	private long endTime;
	private int excelRow;
	private boolean toTest = false;

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getExcelRow() {
		return excelRow;
	}

	public void setExcelRow(int excelRow) {
		this.excelRow = excelRow;
	}

	public boolean isToTest() {
		return toTest;
	}

	public void setToTest(boolean toTest) {
		this.toTest = toTest;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return this.module + " to be executed @  " + this.priority;
	}

	public PriorityExecutor(Integer Priority, String Module, int ExcelRow,
			boolean ToTest) {
		this.priority = Priority;
		this.module = Module;
		this.excelRow = ExcelRow;
		this.toTest = ToTest;
	}

	/**
	 * 
	 * @param inputWorkbook
	 * @return
	 * @throws Exception
	 */
	public static List<PriorityExecutor> getPriorityList(XSSFWorkbook inputWorkbook) throws Exception 
	{
		logger.debug("[Test Batch setup] getPriorityList() execution started");

		String toTest = null;
		String module = null;
		List<PriorityExecutor> priorityList = new ArrayList<PriorityExecutor>();

		module = ExcelHandler.getCellValueX(inputWorkbook, 0, 3, 0);

		for (int row = 3; !module.equals(""); row++) {
			module = ExcelHandler.getCellValueX(inputWorkbook, 0, row, 0);
			if (module.equalsIgnoreCase("End")) {
				break;
			} else if (module.equals("")) {
				throw new InputFileReadException("Sheet 1", row + 1, new NullPointerException());
			}

			toTest = ExcelHandler.getCellValueX(inputWorkbook, 0, row, 1);
			// for priority based execution of the modules.
			// all toTest Modules will be added in Priority
			if (toTest.equalsIgnoreCase("Y")) {
				String priority = ExcelHandler.getCellValueX(inputWorkbook, 0, row, 2);
				// this separate , separated values to priority
				PriorityExecutor prop = null;
				if (priority.contains(",")) {
					String[] priorities = priority.split(",");
					for (String pro : priorities) {
						try {
							Integer actualPrio = (int) Double.parseDouble(pro);
							prop = new PriorityExecutor(actualPrio, module, row, true);
						} catch (NumberFormatException fme) {
							logger.warn("No priority or wrong priority mentioned at line, " + row + ". Execution continued.");
							fme.printStackTrace();
							// no priority or wrong priority is always
							// executed at the end
							prop = new PriorityExecutor(999, module, row, true);
						}
					}
				} else if (priority.length() > 0) {
					try {
						Integer actualPrio = (int) Double.parseDouble(priority);
						prop = new PriorityExecutor(actualPrio, module, row, true);
					} catch (NumberFormatException fe) {
						// no priority or wrong priority is always executed
						// at the end
						logger.warn("No priority or wrong priority mentioned at line, " + row + ". Execution continued.");
						prop = new PriorityExecutor(999, module, row, true);

					}
				} else {
					// no priority or wrong priority is always executed at
					// the end
					prop = new PriorityExecutor(999, module, row, toTest.equalsIgnoreCase("Y"));
				}
				priorityList.add(prop);

			}
		}
		logger.debug("[Test Batch setup] getPriorityList() execution Ended");
		return sortList(priorityList);
	}

	/**
	 * 
	 * @param priorityList
	 * @return
	 */
	private static List<PriorityExecutor> sortList(List<PriorityExecutor> priorityList) {
		logger.debug("[Test Batch setup] sortList execution started");
		// sort according to priority
		Collections.sort(priorityList, new Comparator<PriorityExecutor>() {
			
			public int compare(PriorityExecutor arg0, PriorityExecutor arg1) {
				return arg0.getPriority() - arg1.getPriority();
			}
		});
		logger.debug("[Test Batch setup] sortList execution Ended");
		return priorityList;
	}

}