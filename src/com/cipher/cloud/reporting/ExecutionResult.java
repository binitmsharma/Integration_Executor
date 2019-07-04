package com.cipher.cloud.reporting;

import java.util.ArrayList;
import java.util.List;

public class ExecutionResult {
	private int excelRow;
	private String module;
	private boolean result = true;
	private List<ScenarioResult> scenarioResultList = new ArrayList<ScenarioResult>();
	private String error = "";
	private long totalTimeForExecution;

	public long getTotalTimeForExecution() {
		return totalTimeForExecution;
	}

	public void setTotalTimeForExecution(long totalTimeForExecution) {
		this.totalTimeForExecution = totalTimeForExecution;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setScenarioResultList(List<ScenarioResult> scenarioResultList) {
		this.scenarioResultList = scenarioResultList;
	}

	public int getExcelRow() {
		return excelRow;
	}

	public void setExcelRow(int excelRow) {
		this.excelRow = excelRow;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public List<ScenarioResult> getScenarioResultList() {
		return scenarioResultList;
	}

	public void addScenarioResult(ScenarioResult scenarioResult) {
		this.scenarioResultList.add(scenarioResult);
	}
}
