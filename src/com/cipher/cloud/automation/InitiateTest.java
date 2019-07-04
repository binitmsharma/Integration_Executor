package com.cipher.cloud.automation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cipher.cloud.automation.utils.ExcelHandler;
import com.cipher.cloud.reporting.ExecutionResult;
import com.cipher.cloud.reporting.ReportGenerator;
import com.cipher.cloud.reporting.ScenarioResult;

public class InitiateTest {
	private static Logger logger = Logger.getLogger(InitiateTest.class);
	private XSSFWorkbook inputWorkbook;

	public InitiateTest(XSSFWorkbook inputWorkbook) {
		this.inputWorkbook = inputWorkbook;
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void preparePriorityListAndExecute() throws Exception {
		logger.debug("[Test Batch setup] preparePriorityListAndExecute() execution started");
		List<PriorityExecutor> priorityExecutionList = new ArrayList<PriorityExecutor>();
		priorityExecutionList = PriorityExecutor.getPriorityList(inputWorkbook);

		for (PriorityExecutor priorityExecutor : priorityExecutionList) {
			if (priorityExecutor.isToTest()) {
				handleModuleAutomation(priorityExecutor.getModule());
			}
		}
		logger.debug("[Test Batch tearDown] preparePriorityListAndExecute() execution ended");
	}
	
	/**
	 * 
	 * @param module
	 */
	private void handleModuleAutomation(String module) {
		logger.debug("[Test start] handleModuleAutomation() execution started for test : " + module);

		String scenarioName;
		String toExecute;
		String iteration;
		String mstRepExcelRow;
		int excelRow;

		HandleSeleniumExecution handleSeleniumExecution = new HandleSeleniumExecution();

		ExecutionResult executionResult = new ExecutionResult();
		executionResult.setModule(module);

		excelRow = 5;

		try {
			String browser= null;
			
			scenarioName = ExcelHandler.getCellValueX(inputWorkbook, module, excelRow, 0);
			browser = BrowserConfiguration.openSelenium(scenarioName.split("-")[0], module, inputWorkbook);
			if (executionResult.getExcelRow() != 0 && excelRow < executionResult.getExcelRow()) {
				excelRow = executionResult.getExcelRow();
			}
			
			
			
			do {
				ScenarioResult scenarioResult = null;

				

				//scenarioName = ExcelHandler.getCellValueX(inputWorkbook, module, excelRow, 0);
				toExecute = ExcelHandler.getCellValueX(inputWorkbook, module, excelRow, 1);
				iteration = ExcelHandler.getCellValueX(inputWorkbook, module, excelRow, 12);
				mstRepExcelRow = ExcelHandler.getCellValueX(inputWorkbook, module, excelRow, 14);
				if(mstRepExcelRow != ""){
					HandleSeleniumExecution.mstRepExcelRow = Integer.parseInt(mstRepExcelRow)-1;
				}
				if(iteration.isEmpty()){
					iteration = "1";
				}
				int iterate = Integer.parseInt(iteration);
				/**
				 This block check that scenario is set to execute or not(Y/N).
				 If set to execute will call executeFlow(...)
				 */
				

				if (toExecute != null && toExecute.equalsIgnoreCase("y")) {
					for(int i=0; i<iterate; i++){
						
						scenarioResult = handleSeleniumExecution.executeFlow(excelRow, module, inputWorkbook, i,browser);
						executionResult.addScenarioResult(scenarioResult);
					}
					/*if (!scenarioResult.isResult()) {
						if (!scenarioResult.getScenarioName().contains("Validation Logic")) {
							executionResult.setResult(false);
							executionResult.setError("Main Application failed. So Skipping validation applications");
							break;
						}
					}*/
				}

				do {
					excelRow += 1;
					scenarioName = ExcelHandler.getCellValueX(inputWorkbook, module, excelRow, 0);
				} while (scenarioName != null && scenarioName.equals(""));
				
			} while (scenarioName != null && !scenarioName.equalsIgnoreCase("end"));
		} catch (Exception e) {
			executionResult.setResult(false);
			executionResult.setError("Test failed with error - " + e);
		}
		handleSeleniumExecution.stopSelenium();

		long totalExecutionTime = 0;
		for (ScenarioResult scenarioResult : executionResult.getScenarioResultList()) {
			totalExecutionTime = totalExecutionTime + scenarioResult.getExecutionTime();
			if (!scenarioResult.isResult()) {
				executionResult.setResult(false);
				executionResult.setError("One or more of the Scenarios failed.");
			}
		}

		executionResult.setTotalTimeForExecution(totalExecutionTime);

		ReportGenerator.generateModuleReport(executionResult);

		logger.debug("[Test end] handleModuleAutomation() execution completed for test : " + module);
	}
}
