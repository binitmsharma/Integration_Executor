package com.cipher.cloud.reporting;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author sighil.sivadas
 * 
 */
public class ScenarioResult {
	private String scenarioName;
	private boolean result = true;
	private String error = "NA";
	private long executionTime;
	private ArrayList<String> screenshotList = new ArrayList<String>();
	

	public ArrayList<String> getScreenshotList() {
		return screenshotList;
	}

	public void addScreenshot(String screenshot) {
		screenshotList.add(screenshot);
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
	
	
	
}