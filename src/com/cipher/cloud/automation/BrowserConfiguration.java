package com.cipher.cloud.automation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;

import com.cipher.cloud.automation.utils.ExcelHandler;
import com.cipher.cloud.exceptions.BrowserNotSpecifiedException;
import com.cipher.cloud.exceptions.IncorrectBrowserSpecifiedException;
import com.cipher.cloud.exceptions.URLNotSpecifiedException;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

public abstract class BrowserConfiguration {

	private static Logger logger = Logger.getLogger(BrowserConfiguration.class);

	protected static int screenshotNumber = 1;
	protected static Selenium selenium = null;

	/**
	 * Close open browsers
	 */
	public static void stopSelenium() {
		logger.debug("[Browser Config] stopSelenium() Attampting to close browser");
		if (selenium != null) {
			WebDriver driver = ((WebDriverBackedSelenium) selenium).getWrappedDriver();
			if (driver != null)
				driver.quit();

			driver = null;
			selenium = null;
		}
		logger.debug("[Browser Config] stopSelenium() Browser closed");
	}

	/**
	 * Open browser and URL
	 * 
	 * @param applicationName
	 * @param module
	 * @param inPutWorkBook
	 * @return browser name
	 * @throws Exception
	 */
	protected static String openSelenium(String applicationName, String module, XSSFWorkbook inPutWorkBook) throws Exception {
		logger.debug("[Browser Config] openSelenium() Attempting to start Selenium");

		String tlimit = "";
	    String browser = "";
		String url = "";

		for (int columns = 0;; columns++) {
			if (applicationName.equalsIgnoreCase(ExcelHandler.getCellValueX(inPutWorkBook, module, 0, columns).trim())) {
				url = ExcelHandler.getCellValueX(inPutWorkBook, module, 1, columns);
				browser = ExcelHandler.getCellValueX(inPutWorkBook, module, 3, columns);
				break;
			}
		}

		if (browser.equals("")) {
			throw new BrowserNotSpecifiedException(applicationName, module);
		} else if (url.equals(""))
			throw new URLNotSpecifiedException(applicationName, module);

		boolean browserSetUpSuccessful = setUp(browser, url, module, inPutWorkBook);
		if (!browserSetUpSuccessful) {
			throw new IncorrectBrowserSpecifiedException(applicationName, module, browser);
		}

		tlimit = ExcelHandler.getCellValueX(inPutWorkBook, "Sheet1", 0, 3).trim() + "000";
		int tlim = Integer.parseInt(tlimit);
		tlim *= 60;
		tlimit = Integer.toString(tlim);
		tlimit = tlimit + "000";
		if(selenium != null){
			selenium.setTimeout(tlimit);
			//binit.mohan remove selenium.open("/");
			selenium.open("");
			selenium.waitForPageToLoad("6000");
		}
		if (browser.equalsIgnoreCase("IE")) {
			ManageWindows.handleWindowAuthentication(inPutWorkBook, ((WebDriverBackedSelenium) selenium).getWrappedDriver());
		}

		logger.debug("[Browser Config] openSelenium() Selenium started successfully");
		return browser;
	}

	/**
	 * Set up the browser
	 * 
	 * @param browser
	 *            Name of the browser
	 * @param url
	 *            URL to be opened
	 * @return return true if the browser is invoked and ready to use
	 */
	private static boolean setUp(String browser, String url, String module, XSSFWorkbook inputWorkbook) {
		DesiredCapabilities capabilities = null;
		WebDriver oldDriver = null;
		WebDriver driver = null;

		/**
		 * Check if any open browsers are present. If true, check if the current
		 * browser is the same as old browser.
		 */
		try {
			boolean oldDriverFound = false;
			if (selenium != null) {
				oldDriver = ((WebDriverBackedSelenium) selenium).getWrappedDriver();
				if (browser.equals("IE") && oldDriver instanceof InternetExplorerDriver) {
					oldDriverFound = true;
				} else if (browser.equals("FF") && oldDriver instanceof FirefoxDriver) {
					oldDriverFound = true;
				} else if (browser.equals("NativeApp")){
					oldDriverFound = false;
				} else if (browser.equals("GC") && oldDriver instanceof ChromeDriver){
					oldDriverFound = true;
				} else if(browser.equals("SF") && oldDriver instanceof SafariDriver){
					oldDriverFound = true;
				} else {
					stopSelenium();
				}
			}

			/**
			 * Use the old browser if the new browser is the same
			 */
			if (oldDriverFound) {
				BrowserConfiguration.selenium = new WebDriverBackedSelenium(oldDriver, url);
				BrowserConfiguration.selenium.windowMaximize();
				BrowserConfiguration.selenium.windowFocus();
				logger.debug("[Browser Config] setup() Using existing browser : " + browser);
			} else {
				/**
				 * Open a new browser if the old browser is different
				 */
				if (browser.equals("IE")) {
					File file = new File("C:\\Selenium\\IEDriverServer.exe");
					capabilities = DesiredCapabilities.internetExplorer();
					capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
					capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, "accept");
							
					System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
					driver = new InternetExplorerDriver(capabilities);
				} else if (browser.equals("FF")) {
					if (oldDriver != null) {
						oldDriver.quit();
					}
					capabilities = DesiredCapabilities.firefox();
					capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, "accept");
					driver = new FirefoxDriver(capabilities);
				} else if(browser.equals("GC")){
					File file = new File("C:\\Selenium\\chromedriver.exe");
					capabilities = DesiredCapabilities.chrome();
					capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, "accept");
					System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
					
					//Download location start
					HashMap<String, Object> chromePrefs = new HashMap();  
			        chromePrefs.put("profile.default_content_settings.popups", Integer.valueOf(0));
			        chromePrefs.put("download.default_directory", UseSelenium.outputLocation);
			        ChromeOptions options = new ChromeOptions();
			        options.setExperimentalOption("prefs", chromePrefs);
			        capabilities.setCapability("acceptSslCerts", true);
			        capabilities.setCapability("chromeOptions", options);
			        //End
					driver = new ChromeDriver(capabilities);
				} else if(browser.equals("SF")) {
					capabilities = DesiredCapabilities.safari();
					capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, "accept");
					driver = new SafariDriver(capabilities);
				}
				else if(browser.equals("NativeApp")) {
					/*String appiumExePath, deviceName, androidVersion, platform;
					System.setProperty("webdriver.chrome.driver", "C://Selenium//chromedriver.exe");
					//appiumExePath = ExcelHandler.getCellValueX(inputWorkbook, module, 1, 0);
					//deviceName = ExcelHandler.getCellValueX(inputWorkbook, module, 1, 0);
					//androidVersion = ExcelHandler.getCellValueX(inputWorkbook, module, 1, 0);
					//platform = ExcelHandler.getCellValueX(inputWorkbook, module, 1, 0);
					//executeScript(appiumExePath);
					capabilities = new DesiredCapabilities();
					File app= new File("C:\\Selenium\\com.salesforce.chatter.apk");
					//capabilities.setCapability("deviceName", deviceName);
					//capabilities.setCapability("deviceName", "080b7e41006991b3");
					//note 3
					capabilities.setCapability("deviceName", "34044a1651d22123");
					capabilities.setCapability(CapabilityType.BROWSER_NAME, ""); //Name of mobile web browser to automate. Should be an empty string if automating an app instead.
					capabilities.setCapability(CapabilityType.VERSION, "5.0");
					capabilities.setCapability("platformName", "Android");
					capabilities.setCapability("newCommandTimeout","240");
					capabilities.setCapability("app",app.getAbsolutePath());
					capabilities.setCapability("app-package", "com.salesforce.chatter"); //Replace with your app's package
					capabilities.setCapability("app-activity", "salesfore.chatter.main"); //Replace with app's Activity
					driver = new RemoteWebDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);*/
					
				}
				
				else {
					return false;
				}
				if(!browser.equals("NativeApp")){
					BrowserConfiguration.selenium = new WebDriverBackedSelenium(driver, url);
					BrowserConfiguration.selenium.windowMaximize();
					if(browser.equalsIgnoreCase("GC")){
						driver.manage().window().maximize();
					}
					BrowserConfiguration.selenium.windowFocus();
					logger.debug("[Browser Config] setup() Opening new browser : " + browser);
				}
			}
		} catch (Exception e) {
			logger.error("Error in setting up the browser. Error is : ", e);
			stopSelenium();
			return false;
		}
		return true;
	}
	
	public void executeScript(String scriptPath) throws IOException
	{
		//scriptPath is the path of the executable
		Runtime.getRuntime().exec(scriptPath);
	}
}