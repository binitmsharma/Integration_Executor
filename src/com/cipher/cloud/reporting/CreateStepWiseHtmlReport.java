package com.cipher.cloud.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;

import com.cipher.cloud.automation.UseSelenium;

public class CreateStepWiseHtmlReport {

	String module= new String();
	/*public CreateStepWiseHtmlReport(String module)
	{
		this.module=module;
	}*/
	String indexHTML = new String();
    String Location = UseSelenium.testOutputLocation.concat("/StepWiseReports/") ;
	/**
	 * 
	 * @param scenarioName
	 * @param module
	 * @return
	 */
    public String createReport(String scenarioName , String module)
	{
		indexHTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\">"
				+ "<title>Execution Result</title></head>"
				+ "<body style=\"width: 1024px; padding-left: 25px;\">"
				+ "<div style=\"border-bottom: solid black 1px;\"><table><tr><td>"
				+ "<img src=\"../Resources/Logo.png\" style=\"height: 50%; width: 50%;\" />"
				+ "</td><td><h3>"+ "Test Case: -"
				+ scenarioName
				+ "</h3></td></tr></table></div><br />";

		indexHTML = indexHTML
				+ "<div><table border=\"1\"><tr style=\"text-align: center;\">"
				+ "<th><div style=\"width: 450px;\">Test Step Name</div></th>"
				+ "<th><div style=\"width: 75px;\">Expected Result</div></th>"
				+ "<th><div style=\"width: 75px;\">Actual result</div></th>"
				+ "<th><div style=\"width: 75px;\">Status</div></th></tr>";

		//Location = Location.concat(module);
		
		generateHTMLFile(indexHTML, Location.concat(module), scenarioName);
		return indexHTML;
	}

	/**
	 * 
	 * @param testStepName
	 * @param expectedValue
	 * @param actualValue
	 * @param testResult
	 * @param scenarioName
	 * @param module
	 */
    public void writeToHtmlReport(String testStepName, String expectedValue, String actualValue, Boolean testResult, String scenarioName, String module)
	{
		String color = "red";
		String status = "Failed";

		if (testResult) {
			color = "green";
			status = "Passed";
		}

			indexHTML =  "<tr style=\"text-indent: 5px;\">"
				+ "<td><div style=\"width: 350px;\">" +testStepName +"</div> </td>"
				+ "<td><div style=\"width: 350px;\">" +expectedValue +"</div> </td>"
				+ "<td><div style=\"width: 350px;\">"
				+ actualValue
				+ "</div> </td>" + "<td><div style=\"background: " + color
				+ "; color: white\">" + status + "</div></td>"
				+ "</tr>";
		

		AppendHTMLFile(indexHTML, Location.concat(module), scenarioName);

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
	 * @param file
	 * @param location
	 * @param fileName
	 */
    private static void AppendHTMLFile(String file, String location,String fileName) {

		PrintWriter writer;

		BufferedWriter bw = null;
		FileWriter fw = null;
		try {

			fw = new FileWriter(location + "/" + fileName+ ".html", true);
			bw = new BufferedWriter(fw);
			writer = new PrintWriter(bw);

			writer.println(file);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}}

}
